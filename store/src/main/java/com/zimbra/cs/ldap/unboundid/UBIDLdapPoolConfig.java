package com.zimbra.cs.ldap.unboundid;

import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.LdapServerConfig.ZimbraLdapConfig;
import com.zimbra.cs.ldap.LdapServerType;

public class UBIDLdapPoolConfig {

	private ZimbraLdapConfig replicaConfig;
	private ZimbraLdapConfig masterConfig;

	private LDAPConnectionPool replicaConnPool;

	public LDAPConnectionPool getMasterConnPool() {
		return masterConnPool;
	}

	public LDAPConnectionPool getReplicaConnPool() {
		return replicaConnPool;
	}

	public ZimbraLdapConfig getMasterConfig() {
		return masterConfig;
	}

	public ZimbraLdapConfig getReplicaConfig() {
		return replicaConfig;
	}

	private LDAPConnectionPool masterConnPool;

	public UBIDLdapPoolConfig(ZimbraLdapConfig masterConfig, LDAPConnectionPool masterConnPool,
			ZimbraLdapConfig replicaConfig, LDAPConnectionPool replicaConnPool) {
		this.replicaConfig = replicaConfig;
		this.masterConfig = masterConfig;
		this.replicaConnPool = replicaConnPool;
		this.masterConnPool = masterConnPool;
	}

	public static synchronized UBIDLdapPoolConfig createNewPool(boolean alwaysUseMaster)
			throws LdapException {
		ZimbraLdapConfig masterConfig = null;
		LDAPConnectionPool masterConnPool = null;
		ZimbraLdapConfig replicaConfig = null;
		LDAPConnectionPool replicaConnPool = null;
		try {
			masterConfig = new ZimbraLdapConfig(LdapServerType.MASTER);
			masterConnPool = LdapConnectionPool.createConnectionPool(
					LdapConnectionPool.CP_ZIMBRA_MASTER, masterConfig);
		} catch (LdapException e) {
			ZimbraLog.ldap.info("master is down, falling back to replica...");
			replicaConfig = new ZimbraLdapConfig(LdapServerType.REPLICA);
			replicaConnPool = LdapConnectionPool.createConnectionPool(
					LdapConnectionPool.CP_ZIMBRA_REPLICA, replicaConfig);
			ZimbraLog.ldap.info("using replica");
		}

		if (alwaysUseMaster) {
			replicaConfig = masterConfig;
			replicaConnPool = masterConnPool;
		} else {
			if (replicaConfig == null) {
				replicaConfig = new ZimbraLdapConfig(LdapServerType.REPLICA);
			}
			if (replicaConnPool == null) {
				replicaConnPool = LdapConnectionPool.createConnectionPool(
						LdapConnectionPool.CP_ZIMBRA_REPLICA, replicaConfig);
			}
		}
		return new UBIDLdapPoolConfig(masterConfig, masterConnPool, replicaConfig, replicaConnPool);
	}

	// only called from TestLdapConnectivity unittest, so we can run multiple
	// ldap configs (ldap, ldaps, etc) in one VM
	public synchronized void shutdown() {
		LdapConnectionPool.closeAll();
	}

	synchronized void setReplicaToMasterPool() {
		replicaConnPool = masterConnPool;
	}
}
