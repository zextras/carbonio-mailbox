/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import java.util.Set;

public interface Provisioning {

	String DEFAULT_COS_NAME = "default";
	String DEFAULT_EXTERNAL_COS_NAME = "defaultExternal";

	Config getConfig();

	void getAttrsInOCs(String[] extraObjectClasses, Set<String> attrsInOCs);
}
