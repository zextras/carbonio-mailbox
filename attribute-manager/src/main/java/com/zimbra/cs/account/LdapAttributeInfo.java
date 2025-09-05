/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

public class LdapAttributeInfo {
	private final AttributeInfo attributeInfo;

	public static LdapAttributeInfo get(AttributeInfo attributeInfo) {
		return new LdapAttributeInfo(attributeInfo);
	}

	private LdapAttributeInfo(AttributeInfo attributeInfo) {
		this.attributeInfo = attributeInfo;
	}

	public AttributeCallback getCallback() {
		try {
			return (AttributeCallback) Class.forName(attributeInfo.getCallbackClassName())
					.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
}
