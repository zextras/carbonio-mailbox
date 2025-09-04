/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;

public class AttributeException extends ServiceException {

	private AttributeException(String msg, String code, Boolean receiverFault, Throwable cause) {
		super(msg, code, receiverFault, cause);
	}

	public static final String INVALID_ATTR_NAME = "account.INVALID_ATTR_NAME";
	public static final String INVALID_ATTR_VALUE = "account.INVALID_ATTR_VALUE";
	public static final boolean SENDERS_FAULT = false; // client's fault

	public static AttributeException INVALID_ATTR_NAME(String msg, Throwable t) {
		return new AttributeException(msg, INVALID_ATTR_NAME, SENDERS_FAULT, t);
	}

	public static AttributeException INVALID_ATTR_VALUE(String msg, Throwable t) {
		return new AttributeException(msg, INVALID_ATTR_VALUE, SENDERS_FAULT, t);
	}

}
