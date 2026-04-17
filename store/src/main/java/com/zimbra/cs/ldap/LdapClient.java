// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ldap.LdapServerConfig.ExternalLdapConfig;
import com.zimbra.cs.ldap.LdapServerConfig.GenericLdapConfig;
import com.zimbra.cs.ldap.unboundid.UBIDLdapPoolConfig;
import com.zimbra.cs.util.Zimbra;
import java.util.Date;

public class LdapClient {

    private final UBIDLdapPoolConfig poolConfig;

    // TODO: remove this, kept it for legacy zmconfigd, hopefully we will get rid of it
    private static LdapClient zmconfigdLdapClient;
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

    public static synchronized LdapClient createNew() throws LdapException {
        return createNew(false);
    }

    private static synchronized LdapClient getLdapClientForZmconfigd() {
        if (zmconfigdLdapClient == null) {
            try {
                zmconfigdLdapClient = LdapClient.createNew();
            } catch (LdapException e) {
                Zimbra.halt("failed to initialize LDAP client", e);
            }
        }
        return zmconfigdLdapClient;
    }

    public ZLdapContext toZLdapContext(ILdapContext ldapContext) {

        // just a safety check, this should really not happen at this point
        if (ldapContext != null && !(ldapContext instanceof ZLdapContext)) {
            Zimbra.halt("ILdapContext instance is not ZLdapContext",
                ServiceException.FAILURE("internal error, wrong ldap context instance", null));
        }

        return (ZLdapContext)ldapContext;
    }

    public void waitForLdapServer() {
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


    public ZLdapContext getContext(LdapUsage usage) throws ServiceException {
        return this.getContext(LdapServerType.REPLICA, usage);
    }

    public ZLdapContext getContext(LdapServerType serverType, LdapUsage usage)
        throws ServiceException {
        return this.getContextImpl(serverType, usage);
    }

    public ZLdapContext getContext(LdapServerType serverType, boolean useConnPool,
        LdapUsage usage)
        throws ServiceException {
        return this.getContextImpl(serverType, useConnPool, usage);
    }

    /**
     * For configd only.
     */
    public static ZLdapContext getContext(GenericLdapConfig ldapConfig,
            LdapUsage usage)
    throws ServiceException {
        return getLdapClientForZmconfigd().getExternalContextImpl(ldapConfig, usage);
    }

    public ZLdapContext getExternalContext(ExternalLdapConfig ldapConfig,
        LdapUsage usage)
        throws ServiceException {
        return this.getExternalContextImpl(ldapConfig, usage);
    }

    public void closeContext(ZLdapContext lctxt) {
        if (lctxt != null) {
            lctxt.closeContext(false);
        }
    }

    public ZMutableEntry createMutableEntry() {
        return this.createMutableEntryImpl();
    }

    public void externalLdapAuthenticate(String[] urls, boolean wantStartTLS,
            String bindDN, String password, String note)
    throws ServiceException {
        this.externalLdapAuthenticateImpl(urls, wantStartTLS,
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

    public void shutdown() {
        poolConfig.shutdown();
    }

    public void forceUsingMaster() {
        poolConfig.setReplicaToMasterPool();
    }

    protected ZLdapFilterFactory getLdapFilterFactoryInstance() throws LdapException {
        ZLdapFilterFactory.initialize();
        return new ZLdapFilterFactory();
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
