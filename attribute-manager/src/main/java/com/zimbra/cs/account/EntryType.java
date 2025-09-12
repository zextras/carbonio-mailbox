/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

public enum EntryType {
	ENTRY, // a generic entry, only used in extension
	ACCOUNT,
	ALIAS,
	CALRESOURCE,
	COS,
	DATASOURCE,
	DISTRIBUTIONLIST,
	DOMAIN,
	DYNAMICGROUP,
	DYNAMICGROUP_DYNAMIC_UNIT,
	DYNAMICGROUP_STATIC_UNIT,
	GLOBALCONFIG,
	GLOBALGRANT,
	IDENTITY,
	MIMETYPE,
	SERVER,
	SIGNATURE,
	XMPPCOMPONENT,
	HABGROUP,
	ZIMLET,
	ADDRESS_LIST;

	public String getName() {
		return name();
	}
}
