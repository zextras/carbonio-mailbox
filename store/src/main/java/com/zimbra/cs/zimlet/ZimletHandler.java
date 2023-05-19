// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

/**
 * 
 * @author jylee
 *
 */
public interface ZimletHandler {
	String[] match(String document, ZimletConf config) throws ZimletException;
}
