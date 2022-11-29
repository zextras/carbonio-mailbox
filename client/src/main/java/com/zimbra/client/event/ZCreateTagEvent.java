// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.client.ZItem;
import com.zimbra.client.ZTag;

public class ZCreateTagEvent implements ZCreateItemEvent {

    protected ZTag mTag;

    public ZCreateTagEvent(ZTag tag) throws ServiceException {
        mTag = tag;
    }

    /**
     * @return tag id of created tag.
     * @throws com.zimbra.common.service.ServiceException
     */
    public String getId() throws ServiceException {
        return mTag.getId();
    }

    public ZItem getItem() throws ServiceException {
        return mTag;
    }

    public ZTag getTag() {
        return mTag;
    }
    
    public String toString() {
    	return mTag.toString();
    }
}
