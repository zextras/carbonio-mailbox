package com.zextras.ldap;

import java.io.InputStream;

public class LdifProvider {

	public InputStream getConfigLdif() {
		return this.getClass().getResourceAsStream("/config/cn=config.ldif");
	}

	public InputStream getGlobalConfigLdif() {
		return this.getClass().getResourceAsStream("/zimbra_globalconfig.ldif");
	}

	public InputStream getDefaultCOSLdif() {
		return this.getClass().getResourceAsStream("/zimbra_defaultcos.ldif");
	}

	public InputStream getDefaultExternalCOSLdif() {
		return this.getClass().getResourceAsStream("/zimbra_defaultexternalcos.ldif");
	}

	public InputStream getCarbonioLdif() {
		return this.getClass().getResourceAsStream("/carbonio.ldif");
	}

}
