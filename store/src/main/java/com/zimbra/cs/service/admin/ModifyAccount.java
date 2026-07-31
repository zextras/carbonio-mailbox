// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.Key.CacheEntryBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.CacheEntry;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.listeners.AccountListener;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifyAccountRequest;
import com.zimbra.soap.admin.type.CacheEntryType;
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class ModifyAccount extends AdminDocumentHandler {

  private static final String[] TARGET_ACCOUNT_PATH = new String[] {AdminConstants.E_ID};

  @Override
  protected String[] getProxiedAccountPath() {
    return TARGET_ACCOUNT_PATH;
  }

  /** must be careful and only allow modifies to accounts/attrs domain admin has access to */
  @Override
  public boolean domainAuthSufficient(Map context) {
    return true;
  }

  /**
   * @return true - which means accept responsibility for measures to prevent account harvesting by
   *     delegate admins
   */
  @Override
  public boolean defendsAgainstDelegateAdminAccountHarvesting() {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();
    ModifyAccountRequest req = zsc.elementToJaxb(request);
    AuthToken authToken = zsc.getAuthToken();
    String id = req.getId();
    if (null == id) {
      throw ServiceException.INVALID_REQUEST(
          "missing required attribute: " + AdminConstants.E_ID, null);
    }

    Account account = prov.get(AccountBy.id, id, authToken);

    Map<String, Object> attrs = req.getAttrsAsOldMultimap();
    defendAgainstAccountHarvesting(account, AccountBy.id, id, zsc, attrs);

    /*
     * // Note: isDomainAdminOnly *always* returns false for pure ACL based AccessManager // checkQuota is called
     * only for domain based access manager, remove when we // can totally deprecate domain based access manager if
     * (isDomainAdminOnly(zsc)) checkQuota(zsc, account, attrs);
     */

    // check to see if cos is being changed, need right on new cos
    checkCos(zsc, account, attrs);

    Server newServer = null;
    String newServerName = getStringAttrNewValue(Provisioning.A_zimbraMailHost, attrs);
    if (newServerName != null) {
      newServer = Provisioning.getInstance().getServerByName(newServerName);
      defendAgainstServerNameHarvesting(
          newServer, Key.ServerBy.name, newServerName, zsc, Admin.R_listServer);
    }
    String oldStatus = account.getAccountStatus(prov);
    // pass in true to checkImmutable
    prov.modifyAttrs(account, attrs, true);
    if (attrs.containsKey(Provisioning.A_zimbraAccountStatus)) {
      String newStatus = (String) attrs.get(Provisioning.A_zimbraAccountStatus);
      AccountListener.invokeOnStatusChange(account, oldStatus, newStatus);
    }

    // get account again, in the case when zimbraCOSId or zimbraForeignPrincipal
    // is changed, the cache object(he one we are holding on to) would'd been
    // flushed out from cache. Get the account again to get the fresh one.
    account = prov.get(AccountBy.id, id, zsc.getAuthToken());

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {"cmd", "ModifyAccount", "name", account.getName()}, attrs));

    if (newServer != null) {
      checkNewServer(zsc, context, account, newServer);
    }

    Element response = zsc.createElement(AdminConstants.MODIFY_ACCOUNT_RESPONSE);
    ToXML.encodeAccount(response, account);
    return response;
  }

  public static String getStringAttrNewValue(String attrName, Map<String, Object> attrs)
      throws ServiceException {
    Object object = attrs.get(attrName);
    if (object == null) {
      object = attrs.get("+" + attrName);
    }
    if (object == null) {
      object = attrs.get("-" + attrName);
    }
    if (object == null) {
      return null;
    }

    if (!(object instanceof String)) {
      throw ServiceException.PERM_DENIED(
          "can not modify " + attrName + "(single valued attribute)");
    }
    return (String) object;
  }

  private void checkCos(ZimbraSoapContext zsc, Account account, Map<String, Object> attrs)
      throws ServiceException {
    String newCosId = getStringAttrNewValue(Provisioning.A_zimbraCOSId, attrs);
    if (newCosId == null) {
      return; // not changing it
    }

    Provisioning prov = Provisioning.getInstance();
    if (newCosId.equals("")) {
      // they are unsetting it, so check the domain
      Domain domain = prov.getDomain(account);
      if (domain != null) {
        newCosId =
            account.isIsExternalVirtualAccount()
                ? domain.getDomainDefaultExternalUserCOSId()
                : domain.getDomainDefaultCOSId();
        if (newCosId == null) {
          return; // no domain cos, use the default COS, which is available to all
        }
      }
    }

    Cos cos = prov.get(Key.CosBy.id, newCosId);
    if (cos == null) {
      throw AccountServiceException.NO_SUCH_COS(newCosId);
    }

    // call checkRight instead of checkCosRight, because:
    // 1. no domain based access manager backward compatibility issue
    // 2. we only want to check right if we are using pure ACL based access manager.
    checkRight(zsc, cos, Admin.R_assignCos);
  }

  /*
   * if the account's home server is changed as a result of this command and the new server is no longer this server,
   * need to send a flush cache command to the new server so we don't get into the following:
   *
   * account is on server A (this server)
   *
   * on server B: zmprov ma {account} zimbraMailHost B (the ma is proxied to server A; and on server B, the account
   * still appears to be on A)
   *
   * zmprov ma {account} {any attr} {value} ERROR: service.TOO_MANY_HOPS Until the account is expired from cache on
   * server B.
   */
  private void checkNewServer(
      ZimbraSoapContext zsc, Map<String, Object> context, Account acct, Server newServer) {
    try {
      if (!Provisioning.getInstance().onLocalServer(acct)) {
        // in the case when zimbraMailHost is being removed, newServer will be null
        if (newServer != null) {
          SoapProvisioning soapProv = new SoapProvisioning();
          String adminUrl = URLUtil.getAdminURL(newServer, AdminConstants.ADMIN_SERVICE_URI, true);
          soapProv.soapSetURI(adminUrl);
          soapProv.soapZimbraAdminAuthenticate();
          soapProv.flushCache(
              CacheEntryType.account,
              new CacheEntry[] {new CacheEntry(CacheEntryBy.id, acct.getId())});
        }
      }
    } catch (ServiceException e) {
      // ignore any error and continue
      ZimbraLog.mailbox.warn(
          "cannot flush account cache on server "
              + (newServer == null ? "" : newServer.getName())
              + " for "
              + acct.getName(),
          e);
    }
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_assignCos);

    notes.add(
        String.format(
                AdminRightCheckPoint.Notes.MODIFY_ENTRY, Admin.R_modifyAccount.getName(), "account")
            + "\n");

    notes.add(
        "Notes on "
            + Provisioning.A_zimbraCOSId
            + ": "
            + "If setting "
            + Provisioning.A_zimbraCOSId
            + ", needs the "
            + Admin.R_assignCos.getName()
            + " right on the cos."
            + "If removing "
            + Provisioning.A_zimbraCOSId
            + ", needs the "
            + Admin.R_assignCos.getName()
            + " right on the domain default cos. (in domain attribute "
            + Provisioning.A_zimbraDomainDefaultCOSId
            + ").");
    notes.add(
        String.format(
            "When changing %s attribute, %s right on the server identified by new %s is required.",
            Provisioning.A_zimbraMailHost, Admin.R_listServer, Provisioning.A_zimbraMailHost));
  }
}
