/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import java.util.Map;

public class IDNCallback extends AttributeCallback {

	@Override
	public void preModify(CallbackContext context, String attrName, Object attrValue,
			Map attrsToModify, Entry entry) throws ServiceException {

	}

	@Override
	public void postModify(CallbackContext context, String attrName, Entry entry) {

	}
}
