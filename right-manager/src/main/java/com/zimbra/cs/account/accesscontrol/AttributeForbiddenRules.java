package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;
import java.util.Set;

public class AttributeForbiddenRules {

	private static final Set<String> ALWAYS_FORBIDDEN_ATTRS = Set.of("zimbraIsAdminAccount");

	public static void checkForbiddenAttr(String attrName) throws ServiceException {
		if (isForbiddenAttr(attrName)) {
			throw ServiceException.PERM_DENIED("delegated admin is not allowed to modify " + attrName);
		}
	}

	public static boolean isForbiddenAttr(String attrName) {
		return ALWAYS_FORBIDDEN_ATTRS.contains(attrName.toLowerCase());
	}
}
