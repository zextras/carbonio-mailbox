// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ldap.LdapServerConfig.ExternalLdapConfig;
import com.zimbra.cs.ldap.LdapServerConfig.GenericLdapConfig;
import com.zimbra.cs.ldap.unboundid.UBIDLdapPoolConfig;
import com.zimbra.cs.util.Zimbra;
import java.util.Date;

public class LdapClient {

    private static LdapClient ldapClient;
    private static boolean ALWAYS_USE_MASTER = false;

    private final UBIDLdapPoolConfig poolConfig;

    private LdapClient(UBIDLdapPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public static LdapClient createNew(UBIDLdapPoolConfig poolConfig) throws LdapException {
        ZLdapFilterFactory.initialize();
        final ZLdapFilterFactory factory = new ZLdapFilterFactory();
        ZLdapFilterFactory.setInstance(factory);
        return new LdapClient(poolConfig);
    }

    public static LdapClient createNew(boolean alwaysUseMaster) throws LdapException {
        final UBIDLdapPoolConfig poolConfig = UBIDLdapPoolConfig.createNewPool(alwaysUseMaster);
        return createNew(poolConfig);
    }

    @VisibleForTesting
    public static void setInstance(LdapClient client) {
        ldapClient = client;
    }

    public static synchronized LdapClient getInstanceIfLDAPavailable() throws LdapException {
        if (ldapClient == null) {
            ldapClient = createNew(ALWAYS_USE_MASTER);
        }
        return ldapClient;
    }

     private static synchronized LdapClient getInstance() {
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

        // just a safety check, this should really not happen at this point
        if (ldapContext != null && !(ldapContext instanceof ZLdapContext)) {
            Zimbra.halt("ILdapContext instance is not ZLdapContext",
                    ServiceException.FAILURE("internal error, wrong ldap context instance", null));
        }

        return (ZLdapContext)ldapContext;
    }

    public ZLdapContext toZLdapContext(ILdapContext ldapContext) {

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
     */
    public void zimbraLdapAuthenticate(String bindDN, String password)
    throws ServiceException {
        this.zimbraLdapAuthenticateImpl(bindDN, password);
    }

    protected void terminate() {
        poolConfig.shutdown();
    }

    protected void forceUsingMaster() {
        poolConfig.setReplicaToMasterPool();
    }

    protected ZLdapFilterFactory getLdapFilterFactoryInstance() throws LdapException {
        ZLdapFilterFactory.initialize();
        return new ZLdapFilterFactory();
    }

    protected void waitForLdapServerImpl() {
        while (true) {
            ZLdapContext zlc = null;
            try {
                zlc = new ZLdapContext(poolConfig, LdapServerType.REPLICA, LdapUsage.PING);
                break;
            } catch (ServiceException e) {
                // may be called at server startup when logging is not up yet.
                System.err.println(new Date() + ": error communicating with LDAP (will retry)");
                e.printStackTrace();
                try {
                    Thread.sleep(LdapConstants.CHECK_LDAP_SLEEP_MILLIS);
                } catch (InterruptedException ie) {
                }
            } finally {
                if (zlc != null) {
                    zlc.closeContext(false);
                }
            }
        }
    }

    protected ZLdapContext getContextImpl(LdapServerType serverType, LdapUsage usage)
            throws ServiceException {
        return new ZLdapContext(poolConfig, serverType, usage);
    }

    /**
     * useConnPool is always ignored
     */
    protected ZLdapContext getContextImpl(LdapServerType serverType, boolean useConnPool,
            LdapUsage usage) throws ServiceException {
        return getContextImpl(serverType, usage);
    }

    protected ZLdapContext getExternalContextImpl(ExternalLdapConfig config, LdapUsage usage)
            throws ServiceException {
        return new ZLdapContext(config, usage);
    }

    protected ZMutableEntry createMutableEntryImpl() {
        return new ZMutableEntry();
    }

    protected void externalLdapAuthenticateImpl(String[] urls,
            boolean wantStartTLS, String bindDN, String password, String note)
            throws ServiceException {
        ZLdapContext.externalLdapAuthenticate(urls, wantStartTLS,
                bindDN, password, note);
    }

    protected void zimbraLdapAuthenticateImpl(String bindDN, String password)
            throws ServiceException {
        ZLdapContext.zimbraLdapAuthenticate(bindDN, password, poolConfig);
    }
}
