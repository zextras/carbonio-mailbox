/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.authz.resource.dto;

import com.zimbra.cs.account.accesscontrol.Right;

public record RightInfoResponse(String name, String type, boolean userRight, String description) {

	public static RightInfoResponse from(Right right) {
		return new RightInfoResponse(right.getName(), right.getRightType().name(), right.isUserRight(),
				right.getDesc());
	}
}
