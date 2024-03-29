// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.CacheExtension;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.CacheEntry;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.PermissionCache;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.gal.GalGroup;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.util.WebClientL10nUtil;
import com.zimbra.cs.util.WebClientServiceUtil;
import com.zimbra.cs.zimlet.ZimletUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.FlushCacheRequest;
import com.zimbra.soap.admin.message.FlushCacheResponse;
import com.zimbra.soap.admin.type.CacheEntrySelector;
import com.zimbra.soap.admin.type.CacheEntryType;
import com.zimbra.soap.admin.type.CacheSelector;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FlushCache extends AdminDocumentHandler {

  public static final String FLUSH_CACHE = "flushCache";
  public static final String RES_AJXMSG_JS = "/res/AjxMsg.js";

  /** must be careful and only allow deletes domain admin has access to */
  @Override
  public boolean domainAuthSufficient(Map<String, Object> context) {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    FlushCacheRequest req = zsc.elementToJaxb(request);
    doFlushCache(this, context, req);
    return zsc.jaxbToElement(new FlushCacheResponse());
  }

  public static void doFlushCache(
      AdminDocumentHandler handler, Map<String, Object> context, FlushCacheRequest req)
      throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Server localServer = Provisioning.getInstance().getLocalServer();
    handler.checkRight(zsc, context, localServer, Admin.R_flushCache);

    CacheSelector cacheSelector = req.getCache();
    boolean allServers = cacheSelector.isAllServers();
    String[] types = cacheSelector.getTypes().split(",");
    for (String type : types) {
      CacheEntryType cacheType = null;
      try {
        cacheType = CacheEntryType.fromString(type);
        doFlush(context, cacheType, cacheSelector);
      } catch (ServiceException e) {
        if (cacheType == null) {
          // see if it is a registered extension
          CacheExtension ce = CacheExtension.getHandler(type);
          if (ce != null) {
            ce.flushCache();
          } else {
            throw e;
          }
        } else {
          throw e;
        }
      }
    }
    if (allServers) {
      flushCacheOnAllServers(zsc, req);
    }
  }

  public static void doFlush(
      Map<String, Object> context, CacheEntryType cacheType, CacheSelector cacheSelector)
      throws ServiceException {

    String mailURL = Provisioning.getInstance().getLocalServer().getMailURL();
    switch (cacheType) {
      case acl:
        PermissionCache.invalidateCache();
        break;
      case all:
        flushLdapCache(cacheType, cacheSelector);
        Provisioning.getInstance().refreshValidators(); // refresh other bits of cached license data
        break;
      case galgroup:
        GalGroup.flushCache(getCacheEntries(cacheSelector));
        break;
      case uistrings:
        if (WebClientServiceUtil.isServerInSplitMode()) {
          WebClientServiceUtil.flushUistringsCache();
        } else {
          FlushCache.sendFlushRequest(context, mailURL, RES_AJXMSG_JS);
          FlushCache.sendFlushRequest(context, "/zimbraAdmin", RES_AJXMSG_JS);
        }
        break;
      case locale:
        WebClientL10nUtil.flushCache();
        break;
      case license:
        flushLdapCache(
            CacheEntryType.config, cacheSelector); // refresh global config for parsed license
        Provisioning.getInstance().refreshValidators(); // refresh other bits of cached license data
        break;
      case zimlet:
        ZimletUtil.flushDiskCache(context);
        if (!WebClientServiceUtil.isServerInSplitMode()) {
          flushAllZimlets(context);
        }
        // fall through to also flush ldap entries
      default:
        flushLdapCache(cacheType, cacheSelector);
    }
  }

  public static void flushAllZimlets(Map<String, Object> context) {
    FlushCache.sendFlushRequest(context, "/service", "/zimlet/res/all.js");
  }

  private static CacheEntry[] getCacheEntries(CacheSelector cacheSelector) throws ServiceException {
    List<CacheEntrySelector> cacheEntrySelectors = cacheSelector.getEntries();
    if (cacheEntrySelectors.size() < 1) {
      return null;
    }
    CacheEntry[] entries = new CacheEntry[cacheEntrySelectors.size()];
    int i = 0;
    for (CacheEntrySelector cacheEntrySelector : cacheEntrySelectors) {
      entries[i++] =
          new CacheEntry(
              cacheEntrySelector.getBy().toKeyCacheEntryBy(), cacheEntrySelector.getKey());
    }
    return entries;
  }

  private static void flushLdapCache(CacheEntryType cacheType, CacheSelector cacheSelector)
      throws ServiceException {
    CacheEntry[] entries = getCacheEntries(cacheSelector);
    Provisioning.getInstance().flushCache(cacheType, entries);
  }

  public static void sendFlushRequest(
      Map<String, Object> context, String appContext, String resourceUri) {
    ServletContext containerContext = (ServletContext) context.get(SoapServlet.SERVLET_CONTEXT);
    if (containerContext == null) {
      if (ZimbraLog.misc.isDebugEnabled()) {
        ZimbraLog.misc.debug("flushCache: no container context");
      }
      return;
    }
    ServletContext webappContext = containerContext.getContext(appContext);
    RequestDispatcher dispatcher = webappContext.getRequestDispatcher(resourceUri);
    if (dispatcher == null) {
      if (ZimbraLog.misc.isDebugEnabled()) {
        ZimbraLog.misc.debug("flushCache: no dispatcher for " + resourceUri);
      }
      return;
    }

    try {
      if (ZimbraLog.misc.isDebugEnabled()) {
        ZimbraLog.misc.debug("flushCache: sending flush request");
      }
      ServletRequest request = (ServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
      request.setAttribute(FLUSH_CACHE, Boolean.TRUE);
      ServletResponse response = (ServletResponse) context.get(SoapServlet.SERVLET_RESPONSE);
      dispatcher.include(request, response);
    } catch (Throwable t) {
      // ignore error
      if (ZimbraLog.misc.isDebugEnabled()) {
        ZimbraLog.misc.debug("flushCache: " + t.getMessage());
      }
    }
  }

  private static void flushCacheOnAllServers(ZimbraSoapContext zsc, FlushCacheRequest req)
      throws ServiceException {
    req.getCache().setAllServers(false); // make sure we don't go round in loops

    Provisioning prov = Provisioning.getInstance();
    String localServerId = prov.getLocalServer().getId();

    for (Server server : prov.getAllMailClientServers()) {
      if (localServerId.equals(server.getId())) {
        continue;
      }
      Element request = zsc.jaxbToElement(req);
      ZimbraLog.misc.debug("Flushing cache on server: %s", server.getName());
      String adminUrl = URLUtil.getAdminURL(server, AdminConstants.ADMIN_SERVICE_URI);
      SoapHttpTransport mTransport = new SoapHttpTransport(adminUrl);
      mTransport.setAuthToken(zsc.getRawAuthToken());

      try {
        mTransport.invoke(request);
      } catch (ServiceException | IOException e) {
        // log and continue
        ZimbraLog.misc.warn(
            "Encountered exception during FlushCache on server '%s', skip & continue with the next"
                + " server",
            server.getName(), e);
      }
    }
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_flushCache);
  }
}
