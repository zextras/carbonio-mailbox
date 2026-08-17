/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource.dto;

import com.zimbra.cs.account.Cos;

public record CosInfoResponse(String id, String name) {

	public static CosInfoResponse from(Cos cos) {
		return new CosInfoResponse(cos.getId(), cos.getName());
	}
}
