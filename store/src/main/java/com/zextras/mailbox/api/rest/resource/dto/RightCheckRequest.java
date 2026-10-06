/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource.dto;

public record RightCheckRequest(Subject subject, String right, Target target) {

	public record Subject(String token, String accountId) {

	}

	public record Target(String type, String id) {

	}
}
