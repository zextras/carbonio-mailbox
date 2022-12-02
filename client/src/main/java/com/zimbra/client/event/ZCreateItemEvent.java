// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.client.ZItem;

public interface ZCreateItemEvent extends ZCreateEvent {

    public String getId() throws ServiceException;

    /**
     *
     * @return the ZItem, if this event contains the full item, NULL otherwise.
     *
     * @throws ServiceException on error
     */
    public ZItem getItem() throws ServiceException;
}
