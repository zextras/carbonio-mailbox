/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import java.util.Set;

/**
 * Generic Entry attribute
 */
public interface AttributeEntry {
	String[] getMultiAttr(String name);

	Set<String> getMultiAttrSet(String attrName);
}
