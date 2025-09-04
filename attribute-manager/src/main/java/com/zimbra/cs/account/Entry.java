/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import java.util.Set;

public interface Entry {
	public String[] getMultiAttr(String name);

	Set<String> getMultiAttrSet(String attrName);
}
