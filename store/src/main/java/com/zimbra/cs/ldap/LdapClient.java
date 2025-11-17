// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ldap.LdapServerConfig.ExternalLdapConfig;
import com.zimbra.cs.ldap.LdapServerConfig.GenericLdapConfig;
import com.zimbra.cs.ldap.ZSearchScope.ZSearchScopeFactory;
import com.zimbra.cs.ldap.unboundid.UBIDLdapClient;
import com.zimbra.cs.util.Zimbra;

/**
 * @author pshao
 */
public abstract class LdapClient {

    private static LdapClient ldapClient;
    private static boolean ALWAYS_USE_MASTER = false;

    @VisibleForTesting
    public static void setInstance(LdapClient client) {
        ldapClient = client;
    }

    public static synchronized LdapClient getInstanceIfLDAPavailable() throws LdapException {
        if (ldapClient == null) {
            ldapClient = UBIDLdapClient.createNew(ALWAYS_USE_MASTER);
        }
        return ldapClient;
    }

     static synchronized LdapClient getInstance() {
         try {
             LdapClient.getInstanceIfLDAPavailable();
         } catch (LdapException e) {
             Zimbra.halt("failed to initialize LDAP client", e);
         }
         return ldapClient;
     }

    private static synchronized void unsetInstance() {
        ldapClient = null;
    }

    public static synchronized void masterOnly() {
        ALWAYS_USE_MASTER = true;

        if (ldapClient != null) {
            // already initialized
            ldapClient.forceUsingMaster();
        }
    }

    public static void initializeIfLDAPAvailable() throws LdapException {
        LdapClient.getInstanceIfLDAPavailable();
    }

    public static void initialize() {
        LdapClient.getInstance();
    }

    // called from unittest only
    public static void shutdown() {
        LdapClient.getInstance().terminate();
        unsetInstance();
    }

    @Deprecated
    public static ZLdapContext toZLdapContext(
            com.zimbra.cs.account.Provisioning prov, ILdapContext ldapContext) {

        if (!(getInstance() instanceof UBIDLdapClient)) {
            Zimbra.halt("LdapClient instance is not UBIDLdapClient",
                    ServiceException.FAILURE("internal error, wrong ldap context instance", null));
        }

        // just a safety check, this should really not happen at this point
        if (ldapContext != null && !(ldapContext instanceof ZLdapContext)) {
            Zimbra.halt("ILdapContext instance is not ZLdapContext",
                    ServiceException.FAILURE("internal error, wrong ldap context instance", null));
        }

        return (ZLdapContext)ldapContext;
    }

    public ZLdapContext toZLdapContext(ILdapContext ldapContext) {

        if (!(getInstance() instanceof UBIDLdapClient)) {
            Zimbra.halt("LdapClient instance is not UBIDLdapClient",
                ServiceException.FAILURE("internal error, wrong ldap context instance", null));
        }

        // just a safety check, this should really not happen at this point
        if (ldapContext != null && !(ldapContext instanceof ZLdapContext)) {
            Zimbra.halt("ILdapContext instance is not ZLdapContext",
                ServiceException.FAILURE("internal error, wrong ldap context instance", null));
        }

        return (ZLdapContext)ldapContext;
    }

    /*
     * ========================================================
     * static methods just to short-hand the getInstance() call
     * ========================================================
     */
    public static void waitForLdapServer() {
        getInstance().waitForLdapServerImpl();
    }

    @Deprecated
    public static ZLdapContext getContext(LdapUsage usage) throws ServiceException {
        return getContext(LdapServerType.REPLICA, usage);
    }

    public ZLdapContext getInstanceContext(LdapUsage usage) throws ServiceException {
        return this.getInstanceContext(LdapServerType.REPLICA, usage);
    }

    @Deprecated
    public static ZLdapContext getContext(LdapServerType serverType, LdapUsage usage)
    throws ServiceException {
        return getInstance().getContextImpl(serverType, usage);
    }

    public ZLdapContext getInstanceContext(LdapServerType serverType, LdapUsage usage)
        throws ServiceException {
        return this.getContextImpl(serverType, usage);
    }

    @Deprecated
    public static ZLdapContext getContext(LdapServerType serverType, boolean useConnPool,
            LdapUsage usage)
    throws ServiceException {
        return getInstance().getContextImpl(serverType, useConnPool, usage);
    }

    public ZLdapContext getInstanceContext(LdapServerType serverType, boolean useConnPool,
        LdapUsage usage)
        throws ServiceException {
        return this.getContextImpl(serverType, useConnPool, usage);
    }

    /**
     * For zmconfigd only.
     *
     * zmconfigd uses ldapi connection with root LDAP bind DN/password to bind to
     * Zimbra OpenLdap, whereas LdapClient.getContext() methods use LC keys for
     * the URL/bind DN/password.
     *
     * Changing LC keys in  zmconfigd is not an option, because it also uses
     * LdapProvisioing, which uses the LC settings.
     *
     * We could have zmconfigd call getExternalContext with a ExternalLdapConfig, which
     * takes URL/bind DN/password from parameters.  But the name "external" is misleading.
     *
     * This method id just a wrapper around LdapClient.getExternalContext, the sole
     * purpose is masking out the "external" in method/parameter names.
     *
     * @param ldapConfig
     * @param usage
     * @return
     * @throws ServiceException
     */
    public static ZLdapContext getContext(GenericLdapConfig ldapConfig,
            LdapUsage usage)
    throws ServiceException {
        return getInstance().getExternalContextImpl(ldapConfig, usage);
    }

    @Deprecated
    public static ZLdapContext getExternalContext(ExternalLdapConfig ldapConfig,
            LdapUsage usage)
    throws ServiceException {
        return getInstance().getExternalContextImpl(ldapConfig, usage);
    }

    public ZLdapContext getInstanceExternalContext(ExternalLdapConfig ldapConfig,
        LdapUsage usage)
        throws ServiceException {
        return this.getExternalContextImpl(ldapConfig, usage);
    }

    @Deprecated
    public static void closeContext(ZLdapContext lctxt) {
        if (lctxt != null) {
            lctxt.closeContext(false);
        }
    }

    public void closeInstanceContext(ZLdapContext lctxt) {
        if (lctxt != null) {
            lctxt.closeContext(false);
        }
    }

    @Deprecated
    public static ZMutableEntry createMutableEntry() {
        return getInstance().createMutableEntryImpl();
    }

    public ZMutableEntry createInstanceMutableEntry() {
        return this.createMutableEntryImpl();
    }

    public static void externalLdapAuthenticate(String[] urls, boolean wantStartTLS,
            String bindDN, String password, String note)
    throws ServiceException {
        getInstance().externalLdapAuthenticateImpl(urls, wantStartTLS,
                bindDN, password, note);
    }

    /**
     * LDAP authenticate to the Zimbra LDAP server.
     * Used when stored password is not SSHA.
     *
     * @param password
     * @throws ServiceException
     */
    public void zimbraLdapAuthenticate(String bindDN, String password)
    throws ServiceException {
        this.zimbraLdapAuthenticateImpl(bindDN, password);
    }

    protected abstract void terminate();

    protected abstract void forceUsingMaster();

    protected abstract ZSearchScopeFactory getSearchScopeFactoryInstance();

    protected abstract ZLdapFilterFactory getLdapFilterFactoryInstance()
    throws LdapException;

    protected abstract void waitForLdapServerImpl();

    protected abstract ZLdapContext getContextImpl(LdapServerType serverType, LdapUsage usage)
    throws ServiceException;

    protected abstract ZLdapContext getContextImpl(LdapServerType serverType,
            boolean useConnPool, LdapUsage usage)
    throws ServiceException;

    protected abstract ZLdapContext getExternalContextImpl(ExternalLdapConfig ldapConfig,
            LdapUsage usage)
    throws ServiceException;

    protected abstract ZMutableEntry createMutableEntryImpl();

    protected abstract ZSearchControls createSearchControlsImpl(
            ZSearchScope searchScope, int sizeLimit, String[] returnAttrs);

    protected abstract void externalLdapAuthenticateImpl(String[] urls,
            boolean wantStartTLS, String bindDN, String password, String note)
    throws ServiceException;

    protected abstract void zimbraLdapAuthenticateImpl(String bindDN, String password)
    throws ServiceException;
}
