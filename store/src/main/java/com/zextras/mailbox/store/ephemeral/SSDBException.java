/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

public class SSDBException extends RuntimeException {

	public SSDBException(String message, Throwable cause) {
		super(message, cause);
	}
}
