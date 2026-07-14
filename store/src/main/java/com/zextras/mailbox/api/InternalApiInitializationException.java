/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

/** Thrown when an internal API CDI producer cannot obtain a dependency at instantiation time. */
public class InternalApiInitializationException extends RuntimeException {

	public InternalApiInitializationException(Throwable cause) {
		super(cause);
	}
}
