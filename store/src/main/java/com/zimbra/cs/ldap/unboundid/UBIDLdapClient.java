// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap.unboundid;

import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapConstants;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.LdapServerConfig.ExternalLdapConfig;
import com.zimbra.cs.ldap.LdapServerConfig.ZimbraLdapConfig;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.ldap.ZMutableEntry;
import com.zimbra.cs.ldap.ZSearchControls;
import com.zimbra.cs.ldap.ZSearchScope;
import com.zimbra.cs.ldap.ZSearchScope.ZSearchScopeFactory;
import com.zimbra.cs.ldap.unboundid.UBIDSearchScope.UBIDSearchScopeFactory;
import java.util.Date;

public class UBIDLdapClient extends LdapClient {

	private final UBIDLdapPoolConfig poolConfig;

	private UBIDLdapClient(UBIDLdapPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

	public static UBIDLdapClient init(UBIDLdapPoolConfig poolConfig) throws LdapException {
		UBIDLdapFilterFactory.initialize();
		final UBIDLdapFilterFactory ubidLdapFilterFactory = new UBIDLdapFilterFactory();
		ZSearchScope.init(new UBIDSearchScopeFactory());
		ZLdapFilterFactory.setInstance(ubidLdapFilterFactory);
		return new UBIDLdapClient(poolConfig);
	}

	public static UBIDLdapClient init(boolean alwaysUseMaster) throws LdapException {
		final UBIDLdapPoolConfig ldapPoolConfig = UBIDLdapPoolConfig.init(alwaysUseMaster);
		return init(ldapPoolConfig);
	}

	@Override
	protected void terminate() {
		poolConfig.shutdown();
	}

	@Override
	protected void forceUsingMaster() {
		poolConfig.setReplicaToMasterPool();
	}

	@Override
	protected ZSearchScopeFactory getSearchScopeFactoryInstance() {
		return new UBIDSearchScope.UBIDSearchScopeFactory();
	}

	@Override
	protected ZLdapFilterFactory getLdapFilterFactoryInstance()
			throws LdapException {
		UBIDLdapFilterFactory.initialize();
		return new UBIDLdapFilterFactory();
	}

	@Override
	protected void waitForLdapServerImpl() {
		while (true) {
			UBIDLdapContext zlc = null;
			try {
				zlc = new UBIDLdapContext(LdapServerType.REPLICA, LdapUsage.PING);
				break;
			} catch (ServiceException e) {
				// may called at server startup when logging is not up yet.
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

	@Override
	protected ZLdapContext getContextImpl(LdapServerType serverType, LdapUsage usage)
			throws ServiceException {
		return new UBIDLdapContext(serverType, usage);
	}

	/**
	 * useConnPool is always ignored
	 */
	@Override
	protected ZLdapContext getContextImpl(LdapServerType serverType, boolean useConnPool,
			LdapUsage usage)
			throws ServiceException {
		return getContextImpl(serverType, usage);
	}

	@Override
	protected ZLdapContext getExternalContextImpl(ExternalLdapConfig config, LdapUsage usage)
			throws ServiceException {
		return new UBIDLdapContext(config, usage);
	}

	@Override
	protected ZMutableEntry createMutableEntryImpl() {
		return new UBIDMutableEntry();
	}

	@Override
	protected ZSearchControls createSearchControlsImpl(
			ZSearchScope searchScope, int sizeLimit, String[] returnAttrs) {
		return new UBIDSearchControls(searchScope, sizeLimit, returnAttrs);
	}

	@Override
	protected void externalLdapAuthenticateImpl(String[] urls,
			boolean wantStartTLS, String bindDN, String password, String note)
			throws ServiceException {
		UBIDLdapContext.externalLdapAuthenticate(urls, wantStartTLS,
				bindDN, password, note);
	}

	@Override
	protected void zimbraLdapAuthenticateImpl(String bindDN, String password)
			throws ServiceException {
		// TODO: check me
		UBIDLdapContext.zimbraLdapAuthenticate(bindDN, password, poolConfig);
	}

}
