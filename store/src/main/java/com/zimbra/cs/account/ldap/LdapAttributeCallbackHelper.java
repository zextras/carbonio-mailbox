/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeConfig;
import com.zimbra.cs.account.AttributeException;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.LdapAttributeInfo;
import com.zimbra.cs.account.callback.CallbackContext;
import java.util.Map;
import java.util.Set;

public class LdapAttributeCallbackHelper {

	public static LdapAttributeCallbackHelper get(AttributeManager attributeManager) {
		return new LdapAttributeCallbackHelper(attributeManager);
	}

	private final AttributeManager attributeManager;

	public LdapAttributeCallbackHelper(AttributeManager attributeManager) {
		this.attributeManager = attributeManager;
	}

	public void preModify(
			Map<String, ? extends Object> attrs,
			Entry entry,
			CallbackContext context,
			boolean checkImmutable)
			throws ServiceException {
		preModify(attrs, entry, context, checkImmutable, true);
	}

	public void preModify(
			Map<String, ? extends Object> attrs,
			Entry entry,
			CallbackContext context,
			boolean checkImmutable,
			boolean allowCallback)
			throws ServiceException {
		String[] keys = attrs.keySet().toArray(new String[0]);
		for (String key : keys) {
			String name = key;
			if (name.length() == 0) {
				throw AttributeException.INVALID_ATTR_NAME("empty attr name found", null);
			}
			Object value = attrs.get(name);
			if (name.charAt(0) == '-' || name.charAt(0) == '+') {
				name = name.substring(1);
			}
			AttributeInfo info = attributeManager.getmAttrs().get(name.toLowerCase());
			if (info != null) {
				if (info.isDeprecated()) {
					ZimbraLog.misc.warn("Attempt to modify a deprecated attribute: " + name);
				}

				// IDN unicode to ACE conversion needs to happen before checkValue or else
				// regex attrs will be rejected by checkValue
				// NOTE: idnCallback does nothing, it was an empty method
				if (attributeManager.idnType(name).isEmailOrIDN()) {
					value = attrs.get(name);
				}
				LdapAttributeInfo.get(info).checkValue(value, checkImmutable, attrs);
				final LdapAttributeInfo ldapAttributeInfo = LdapAttributeInfo.get(info);
				if (allowCallback && ldapAttributeInfo.getCallback() != null) {
					ldapAttributeInfo.getCallback().preModify(context, name, value, attrs, entry);
				}
			} else {
				ZimbraLog.misc.warn("checkValue: no attribute info for: " + name);
			}
		}
	}

	public void postModify(
			Map<String, ? extends Object> attrs, Entry entry, CallbackContext context) {
		postModify(attrs, entry, context, true);
	}

	public void postModify(
			Map<String, ? extends Object> attrs,
			Entry entry,
			CallbackContext context,
			boolean allowCallback) {
		String[] keys = attrs.keySet().toArray(new String[0]);
		for (String key : keys) {
			String name = key;
			if (name.charAt(0) == '-' || name.charAt(0) == '+') {
				name = name.substring(1);
			}
			AttributeInfo info = attributeManager.getmAttrs().get(name.toLowerCase());

			final LdapAttributeInfo ldapAttributeInfo = LdapAttributeInfo.get(info);
			if (info != null && (allowCallback && ldapAttributeInfo.getCallback() != null)) {
				try {
					ldapAttributeInfo.getCallback().postModify(context, name, entry);
				} catch (Exception e) {
					// need to swallow all exceptions as postModify shouldn't throw any...
					ZimbraLog.account.warn("postModify caught exception: " + e.getMessage(), e);
				}
			}
		}
	}

	private void getExtraObjectClassAttrs(
			LdapProv ldapProv, AttributeClass attrClass, String extraObjectClassAttr)
			throws ServiceException {
		AttributeConfig config = ldapProv.getConfig();

		String[] extraObjectClasses = config.getMultiAttr(extraObjectClassAttr);

		if (extraObjectClasses.length > 0) {
			Set<String> attrsInOCs = attributeManager.getmClassToAttrsMap().get(AttributeClass.account);
			ldapProv.getAttrsInOCs(extraObjectClasses, attrsInOCs);
		}
	}

	private void getLdapSchemaExtensionAttrs(LdapProv repository) throws ServiceException {
		if (attributeManager.ismLdapSchemaExtensionInited()) return;

		attributeManager.setmLdapSchemaExtensionInited(true);

		this.getExtraObjectClassAttrs(
				repository, AttributeClass.account, "zimbraAccountExtraObjectClass");
		this.getExtraObjectClassAttrs(
				repository,
				AttributeClass.calendarResource,
				"zimbraCalendarResourceExtraObjectClass");
		this.getExtraObjectClassAttrs(
				repository, AttributeClass.cos, "zimbraCosExtraObjectClass");
		this.getExtraObjectClassAttrs(
				repository, AttributeClass.domain, "zimbraDomainExtraObjectClass");
		this.getExtraObjectClassAttrs(
				repository, AttributeClass.server, "zimbraServerExtraObjectClass");
	}
	public void loadLdapSchemaExtensionAttrs(LdapProv ldapProv) {
		synchronized (AttributeManager.class) {
			try {
				this.getLdapSchemaExtensionAttrs(ldapProv);
				attributeManager.computeClassToAllAttrsMap(); // recompute the ClassToAllAttrsMap
			} catch (ServiceException e) {
				ZimbraLog.account.warn("unable to load LDAP schema extensions", e);
			}
		}
	}
}
