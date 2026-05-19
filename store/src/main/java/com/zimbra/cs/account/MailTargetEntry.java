package com.zimbra.cs.account;

import java.util.Map;

public abstract class MailTargetEntry extends MailTarget {

	public MailTargetEntry(String name, String id, Map<String, Object> attrs,
			Map<String, Object> defaults, Provisioning prov) {
		super(name, id, attrs, defaults, prov);
	}

	public Provisioning getProvisioningInternal() {
		return null;
	}
}
