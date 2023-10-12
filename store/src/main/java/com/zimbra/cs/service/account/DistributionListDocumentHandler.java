// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.Key.CacheEntryBy;
import com.zimbra.common.account.Key.ServerBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.CacheEntry;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.soap.admin.type.CacheEntryType;
import java.util.List;
import java.util.Map;

/**
 * @author pshao
 */
public abstract class DistributionListDocumentHandler extends AccountDocumentHandler {

  /*
   * Centralized callsite for adding/removing group members in DistributionListAction.
   * The group object passed in is a "basic" group instance,
   * obtained from Provisioning.getGroupBasic().  Unlink "full" group instances, basic
   * instances don't contain all attributes, and basic groups are cached in LdapProvisioning.
   * For dynamic groups, there is no difference between basic and full instances of a group.
   * For static groups, the basic instance does not contain member attribute of the group.
   * We have to load a full instance, and pass it to Provisiioning.add/removeGroupMembers.
   *
   * Note: loading full instance of a static group will always make a trip to LDAP,
   *       these instances are *not* cahed.
   */
  protected static void addGroupMembers(Provisioning prov, Group group, String[] members)
      throws ServiceException {
    group = loadFullGroupFromMaster(prov, group);
    prov.addGroupMembers(group, members);

    // flush account from cache for internal members
    flushAccountCache(prov, members);
  }

  protected static void removeGroupMembers(Provisioning prov, Group group, String[] members)
      throws ServiceException {
    group = loadFullGroupFromMaster(prov, group);
    prov.removeGroupMembers(group, members);

    // flush account from cache for internal members
    flushAccountCache(prov, members);
  }

  private static Group loadFullGroupFromMaster(Provisioning prov, Group group)
      throws ServiceException {
    if (!group.isDynamic()) {
      String groupName = group.getName();

      // load full instance of the static group.
      // note: this always cost a LDAP search

      // bug 72482: load the group from LDAP master.  For delegated groups,
      // client issues a BatchRequest containing CreateDistributionList and
      // DistributionListAction addMembers requests.  If the newly created DL has
      // not synced to replica yet, the prov.getGroup will trturn null.
      group = prov.getGroup(Key.DistributionListBy.id, group.getId(), true, false);

      if (group == null) {
        throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(groupName);
      }
    }

    return group;
  }

  private static void flushAccountCache(Provisioning prov, String[] members) {
    // List<CacheEntry> localAccts = Lists.newArrayList();
    Map<String /* server name */, List<CacheEntry>> remoteAccts = Maps.newHashMap();

    for (String member : members) {
      try {
        Account acct = prov.get(AccountBy.name, member);
        if (acct != null) {
          if (Provisioning.getInstance().onLocalServer(acct)) {
            // localAccts.add(new CacheEntry(CacheEntryBy.id, acct.getId()));
          } else {
            Server server = acct.getServer();
            String serverName = server.getName();
            List<CacheEntry> acctsOnServer = remoteAccts.get(serverName);
            if (acctsOnServer == null) {
              acctsOnServer = Lists.newArrayList();
              remoteAccts.put(serverName, acctsOnServer);
            }
            acctsOnServer.add(new CacheEntry(CacheEntryBy.id, acct.getId()));
          }
        }

        // else, not internal account, skip

      } catch (ServiceException e) {
        // log and continue
        ZimbraLog.account.warn("unable to flush account cache", e);
      }
    }

    /*
     * No need to flush cache on local server, account membership for static/dynamic
     * groups are handled in LdapProvisioning
     *
    // flush accounts from cache on local server
    try {
        prov.flushCache(CacheEntryType.account, localAccts.toArray(new CacheEntry[localAccts.size()]));
    } catch (ServiceException e) {
        // log and continue
        ZimbraLog.account.warn("unable to flush account cache on local server", e);
    }
    */

    // flush accounts from cache on remote servers
    // if the remote server does not run admin server, too bad - accounts on that
    // server will have to wait till cache expire to get updated membership
    SoapProvisioning soapProv = new SoapProvisioning();
    String adminUrl = null;
    for (Map.Entry<String, List<CacheEntry>> acctsOnServer : remoteAccts.entrySet()) {
      String serverName = acctsOnServer.getKey();
      List<CacheEntry> accts = acctsOnServer.getValue();

      try {
        Server server = prov.get(ServerBy.name, serverName);
        adminUrl = URLUtil.getAdminURL(server, AdminConstants.ADMIN_SERVICE_URI, true);
        soapProv.soapSetURI(adminUrl);
        soapProv.soapZimbraAdminAuthenticate();
        soapProv.flushCache(CacheEntryType.account, accts.toArray(new CacheEntry[accts.size()]));

      } catch (ServiceException e) {
        ZimbraLog.account.warn("unable to flush account cache on remote server: " + serverName, e);
      }
    }
  }

  @Override
  protected Element proxyIfNecessary(Element request, Map<String, Object> context)
      throws ServiceException {
    try {
      Group group = getGroupBasic(request, Provisioning.getInstance());

      if (!Provisioning.onLocalServer(group)) {
        Server server = group.getServer();
        if (server == null) {
          throw ServiceException.PROXY_ERROR(
              AccountServiceException.NO_SUCH_SERVER(group.getAttr(Provisioning.A_zimbraMailHost)),
              "");
        }
        return proxyRequest(request, context, server);
      } else {
        // execute locally
        return null;
      }
    } catch (ServiceException e) {
      // must be able to proxy, we don't want to fallback to local
      throw e;
    }
  }

  protected Group getGroupBasic(Element request, Provisioning prov) throws ServiceException {
    Element eDL = request.getElement(AccountConstants.E_DL);
    String key = eDL.getAttribute(AccountConstants.A_BY);
    String value = eDL.getText();

    Group group = prov.getGroupBasic(Key.DistributionListBy.fromString(key), value);

    if (group == null) {
      throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(value);
    }

    return group;
  }

  protected abstract static class SynchronizedGroupHandler {

    protected Group group;

    protected SynchronizedGroupHandler(Group group) {
      this.group = group;
    }

    protected abstract void handleRequest() throws ServiceException;

    protected void handle() throws ServiceException {
      synchronized (group) {
        handleRequest();
      }
    }
  }
}
