// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeFlag;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.Signature;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.service.admin.AdminAccessControl;
import com.zimbra.cs.session.Session;
import com.zimbra.cs.session.SoapSession;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.cs.zimlet.ZimletPresence;
import com.zimbra.cs.zimlet.ZimletUserProperties;
import com.zimbra.cs.zimlet.ZimletUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.type.Prop;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @since May 26, 2004
 * @author schemers
 */
public class GetInfo extends AccountDocumentHandler  {

    public interface GetInfoExt {
        void handle(ZimbraSoapContext zsc, Element getInfoResponse);
    }

    private static ArrayList<GetInfoExt> extensions = new ArrayList<>();

    public static void addExtension(GetInfoExt extension) {
        synchronized (extensions) {
            extensions.add(extension);
        }
    }

    private enum Section {
        MBOX, PREFS, ATTRS, ZIMLETS, PROPS, IDENTS, SIGS, DSRCS, CHILDREN;

        static Section lookup(String value) throws ServiceException {
            try {
                return Section.valueOf(value.toUpperCase().trim());
            } catch (IllegalArgumentException iae) {
                throw ServiceException.INVALID_REQUEST("unknown GetInfo section: " + value.trim(), null);
            }
        }
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);
        if (!canAccessAccount(zsc, account)) {
            throw ServiceException.PERM_DENIED("can not access account");
        }

        // figure out the subset of data the caller wants (default to all data)
        String secstr = request.getAttribute(AccountConstants.A_SECTIONS, null);
        Set<Section> sections;
        if (!StringUtil.isNullOrEmpty(secstr)) {
            sections = EnumSet.noneOf(Section.class);
            for (String sec : Splitter.on(',').omitEmptyStrings().trimResults().split(secstr)) {
                sections.add(Section.lookup(sec));
            }
        } else {
            sections = EnumSet.allOf(Section.class);
        }

        String rightsStr = request.getAttribute(AccountConstants.A_RIGHTS, null);
        Set<Right> rights = null;
        if (!StringUtil.isNullOrEmpty(rightsStr)) {
            RightManager rightMgr = RightManager.getInstance();
            rights = Sets.newHashSet();
            for (String right : Splitter.on(',').omitEmptyStrings().trimResults().split(rightsStr)) {
                rights.add(rightMgr.getUserRight(right));
            }
        }


        Element response = zsc.createElement(AccountConstants.GET_INFO_RESPONSE);
        response.addAttribute(AccountConstants.E_VERSION, BuildInfo.FULL_VERSION, Element.Disposition.CONTENT);
        response.addAttribute(AccountConstants.E_ID, account.getId(), Element.Disposition.CONTENT);
        response.addAttribute(AccountConstants.E_NAME, account.getUnicodeName(), Element.Disposition.CONTENT);
        try {
            response.addAttribute(AccountConstants.E_CRUMB, zsc.getAuthToken().getCrumb(), Element.Disposition.CONTENT);
        } catch (AuthTokenException e) {
            // shouldn't happen
            ZimbraLog.account.warn("can't generate crumb", e);
        }
        long lifetime = zsc.getAuthToken().getExpires() - System.currentTimeMillis();
        response.addAttribute(AccountConstants.E_LIFETIME, lifetime, Element.Disposition.CONTENT);

        Provisioning prov = Provisioning.getInstance();

        // bug 53770, return if the request is using a delegated authtoken issued to an admin account
        AuthToken authToken = zsc.getAuthToken();
        if (authToken.isDelegatedAuth()) {
            Account admin = prov.get(AccountBy.id, authToken.getAdminAccountId());
            if (admin != null) {
                boolean isAdmin = AdminAccessControl.isAdequateAdminAccount(admin);
                if (isAdmin) {
                    response.addAttribute(AccountConstants.E_ADMIN_DELEGATED, true, Element.Disposition.CONTENT);
                }
            }
        }

        try {
            Server server = prov.getLocalServer();
            if (server != null) {
                response.addAttribute(AccountConstants.A_DOCUMENT_SIZE_LIMIT, server.getFileUploadMaxSize());
            }
            Config config = prov.getConfig();
            if (config != null) {
                long maxAttachSize = config.getMtaMaxMessageSize();
                if (maxAttachSize == 0) {
                    maxAttachSize = -1;  /* means unlimited */
                }
                response.addAttribute(AccountConstants.A_ATTACHMENT_SIZE_LIMIT, maxAttachSize);
            }
        } catch (ServiceException e) {
        }

    if (sections.contains(Section.MBOX) && Provisioning.getInstance().onLocalServer(account)) {
      response.addAttribute(
          AccountConstants.E_REST, UserServlet.getRestUrl(account), Element.Disposition.CONTENT);
      response.addAttribute(
          AccountConstants.E_QUOTA_USED, mbox.getSize(), Element.Disposition.CONTENT);
      response.addAttribute(
          AccountConstants.E_IS_TRACKING_IMAP, mbox.isTrackingImap(), Element.Disposition.CONTENT);

                Session s = (Session) context.get(SoapEngine.ZIMBRA_SESSION);
                if (s instanceof SoapSession) {
                    // we have a valid session; get the stats on this session
                    response.addAttribute(AccountConstants.E_PREVIOUS_SESSION,
                            ((SoapSession) s).getPreviousSessionTime(), Element.Disposition.CONTENT);
                    response.addAttribute(AccountConstants.E_LAST_ACCESS,
                            ((SoapSession) s).getLastWriteAccessTime(), Element.Disposition.CONTENT);
                    response.addAttribute(AccountConstants.E_RECENT_MSGS,
                            ((SoapSession) s).getRecentMessageCount(), Element.Disposition.CONTENT);
                } else {
                    // we have no session; calculate the stats from the mailbox and the other SOAP sessions
                    long lastAccess = mbox.getLastSoapAccessTime();
                    response.addAttribute(AccountConstants.E_PREVIOUS_SESSION, lastAccess, Element.Disposition.CONTENT);
                    response.addAttribute(AccountConstants.E_LAST_ACCESS, lastAccess, Element.Disposition.CONTENT);
                    response.addAttribute(AccountConstants.E_RECENT_MSGS,
                            mbox.getRecentMessageCount(), Element.Disposition.CONTENT);
                }
        }

        doCos(account, response);

        Map<String, Object> attrMap = account.getUnicodeAttrs();
        Locale locale = Provisioning.getInstance().getLocale(account);

        if (sections.contains(Section.PREFS)) {
            Element prefs = response.addUniqueElement(AccountConstants.E_PREFS);
            GetPrefs.doPrefs(account, prefs, attrMap, null);
        }
        if (sections.contains(Section.ATTRS)) {
            Element attrs = response.addUniqueElement(AccountConstants.E_ATTRS);
            doAttrs(account, locale.toString(), attrs, attrMap);
        }
        if (sections.contains(Section.ZIMLETS)) {
            Element zimlets = response.addUniqueElement(AccountConstants.E_ZIMLETS);
            doZimlets(zimlets, account);
        }
        if (sections.contains(Section.PROPS)) {
            Element props = response.addUniqueElement(AccountConstants.E_PROPERTIES);
            doProperties(props, account);
        }
        if (sections.contains(Section.IDENTS)) {
            Element ids = response.addUniqueElement(AccountConstants.E_IDENTITIES);
            doIdentities(ids, account);
        }
        if (sections.contains(Section.SIGS)) {
            Element sigs = response.addUniqueElement(AccountConstants.E_SIGNATURES);
            doSignatures(sigs, account);
        }
        if (sections.contains(Section.DSRCS)) {
            Element ds = response.addUniqueElement(AccountConstants.E_DATA_SOURCES);
            doDataSources(ds, account);
        }

        if (rights != null && !rights.isEmpty()) {
            Element eRights = response.addUniqueElement(AccountConstants.E_RIGHTS);
            doDiscoverRights(eRights, account, rights);
        }

        GetAccountInfo.addUrls(response, account);

        for (GetInfoExt extension : extensions) {
            extension.handle(zsc, response);
        }

        // we do not have any ldap attrs to define whether pasteitcleaned service is installed and running
        // so check if pasteitcleaned service is installed and running by checking the installed directory and connectivity
        // if yes return pasteitcleanedEnabled = true else return pasteitcleanedEnabled = false
        response.addAttribute("pasteitcleanedEnabled", checkIfPasteitcleanedInstalled(), Element.Disposition.CONTENT);

        return response;
    }

    static void doCos(Account acct, Element response) throws ServiceException {
        Cos cos = Provisioning.getInstance().getCOS(acct);
        if (cos != null) {
            Element eCos = response.addUniqueElement(AccountConstants.E_COS);
            eCos.addAttribute(AccountConstants.A_ID, cos.getId());
            eCos.addAttribute(AccountConstants.A_NAME, cos.getName());
        }
    }

    static void doAttrs(Account acct, String locale, Element response, Map<String,Object> attrsMap)
            throws ServiceException {
        AttributeManager attrMgr = AttributeManager.getInstance();

        Set<String> attrList = attrMgr.getAttrsWithFlag(AttributeFlag.accountInfo);

        Set<String> acctAttrs = attrMgr.getAllAttrsInClass(AttributeClass.account);
        Set<String> domainAttrs = attrMgr.getAllAttrsInClass(AttributeClass.domain);
        Set<String> serverAttrs = attrMgr.getAllAttrsInClass(AttributeClass.server);
        Set<String> configAttrs = attrMgr.getAllAttrsInClass(AttributeClass.globalConfig);

        Provisioning prov = Provisioning.getInstance();
        Domain domain = prov.getDomain(acct);
        Server server = acct.getServer();
        Config config = prov.getConfig();

        for (String key : attrList) {
            Object value = null;
            if (Provisioning.A_zimbraLocale.equals(key)) {
                value = locale;
            } else if (Provisioning.A_zimbraAttachmentsBlocked.equals(key)) {
                // leave this a special case for now, until we have enough incidences to make it a pattern
                value = config.isAttachmentsBlocked() || acct.isAttachmentsBlocked() ?
                        ProvisioningConstants.TRUE : ProvisioningConstants.FALSE;
            } else {
                value = attrsMap.get(key);

                if (value == null) { // no value on account/cos
                    if (!acctAttrs.contains(key)) { // not an account attr
                        // see if it is on domain, server, or globalconfig
                        if (domainAttrs.contains(key)) {
                            if (domain != null) {
                                value = domain.getMultiAttr(key); // value on domain/global config (domainInherited)
                            }
                        } else if (serverAttrs.contains(key)) {
                            value = server.getMultiAttr(key); // value on server/global config (serverInherited)
                        } else if (configAttrs.contains(key)) {
                            value = config.getMultiAttr(key); // value on global config
                        }
                    }
                }
            }

            ToXML.encodeAttr(response, key, value);
        }
    }

    private static void doZimlets(Element response, Account acct) {
        try {
            // bug 34517
            ZimletUtil.migrateUserPrefIfNecessary(acct);

            ZimletPresence userZimlets = ZimletUtil.getUserZimlets(acct);
            List<Zimlet> zimletList = ZimletUtil.orderZimletsByPriority(userZimlets.getZimletNamesAsArray());
            int priority = 0;
            for (Zimlet z : zimletList) {
                if (z.isEnabled() && !z.isExtension()) {
                    ZimletUtil.listZimlet(response, z, priority, userZimlets.getPresence(z.getName()));
                }
                priority++;
            }

            // load the zimlets in the dev directory and list them
            ZimletUtil.listDevZimlets(response);
        } catch (ServiceException se) {
            ZimbraLog.account.error("can't get zimlets", se);
        }
    }

    private static void doProperties(Element response, Account acct) {
        ZimletUserProperties zp = ZimletUserProperties.getProperties(acct);
        Set<? extends Prop> props = zp.getAllProperties();
        for (Prop prop : props) {
            Element elem = response.addElement(AccountConstants.E_PROPERTY);
            elem.addAttribute(AccountConstants.A_ZIMLET, prop.getZimlet());
            elem.addAttribute(AccountConstants.A_NAME, prop.getName());
            elem.setText(prop.getValue());
        }
    }

    private static void doIdentities(Element response, Account acct) {
        try {
            for (Identity i : Provisioning.getInstance().getAllIdentities(acct)) {
                ToXML.encodeIdentity(response, i);
            }
        } catch (ServiceException e) {
            ZimbraLog.account.error("can't get identities", e);
        }
    }

    private static void doSignatures(Element response, Account acct) {
        try {
            List<Signature> signatures = Provisioning.getInstance().getAllSignatures(acct);
            for (Signature s : signatures) {
                ToXML.encodeSignature(response, s);
            }
        } catch (ServiceException e) {
            ZimbraLog.account.error("can't get signatures", e);
        }
    }

    private static void doDataSources(Element response, Account acct) {
        try {
            List<DataSource> dataSources = Provisioning.getInstance().getAllDataSources(acct);
            for (DataSource ds : dataSources) {
                if (!ds.isInternal()) {
                    com.zimbra.cs.service.mail.ToXML.encodeDataSource(response, ds);
                }
            }
        } catch (ServiceException e) {
            ZimbraLog.mailbox.error("Unable to get data sources", e);
        }
    }

    protected Element encodeChildAccount(Element parent, Account child, boolean isVisible) {
        Element elem = parent.addElement(AccountConstants.E_CHILD_ACCOUNT);
        elem.addAttribute(AccountConstants.A_ID, child.getId());
        elem.addAttribute(AccountConstants.A_NAME, child.getUnicodeName());
        elem.addAttribute(AccountConstants.A_VISIBLE, isVisible);
        elem.addAttribute(AccountConstants.A_ACTIVE, child.isAccountStatusActive());

        String displayName = child.getAttr(Provisioning.A_displayName);
        if (displayName != null) {
            Element attrsElem = elem.addUniqueElement(AccountConstants.E_ATTRS);
            attrsElem.addKeyValuePair(Provisioning.A_displayName, displayName,
                    AccountConstants.E_ATTR, AccountConstants.A_NAME);
        }
        return elem;
    }

    private void doDiscoverRights(Element eRights, Account account, Set<Right> rights) throws ServiceException {
        DiscoverRights.discoverRights(account, rights, eRights, false);
    }

    private boolean checkIfPasteitcleanedInstalled() {
        String libLocation = "/opt/zextras/common/lib/pasteitcleaned";
        String extLoc = "/opt/zextras/lib/ext/pasteitcleaned/pasteitcleaned.jar";
        HttpURLConnection connection = null;
        try {
            File lib = new File(libLocation);
            File ext = new File(extLoc);
            if (lib.exists() && lib.isDirectory() && lib.canRead()) {
                if (ext.exists() && ext.isFile() && ext.canRead()) {
                    connection = (HttpURLConnection) new URL("http://localhost:5000").openConnection();
                    connection.setConnectTimeout(1000);
                    connection.connect();
                    return true;
                }
            }
            ZimbraLog.account.debug("pasteitcleaned service is not installed or running");
            return false;
        } catch(InvalidPathException | SecurityException | IOException ex) {
            ZimbraLog.account.info("exception occurred : %s", ex.getMessage());
            return false;
        }
    }
}
