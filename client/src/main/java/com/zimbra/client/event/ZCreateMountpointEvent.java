// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.client.ZMountpoint;

public class ZCreateMountpointEvent implements ZCreateItemEvent {

    protected ZMountpoint mMountpoint;

    public ZCreateMountpointEvent(ZMountpoint mountpoint) throws ServiceException {
        mMountpoint = mountpoint;
    }

	public String toString() {
    	return mMountpoint.toString();
    }
}
