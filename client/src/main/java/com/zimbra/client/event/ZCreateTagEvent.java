// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.client.ZTag;

public class ZCreateTagEvent implements ZCreateItemEvent {

    protected ZTag mTag;

    public ZCreateTagEvent(ZTag tag) throws ServiceException {
        mTag = tag;
    }

	public ZTag getTag() {
        return mTag;
    }
    
    public String toString() {
    	return mTag.toString();
    }
}
