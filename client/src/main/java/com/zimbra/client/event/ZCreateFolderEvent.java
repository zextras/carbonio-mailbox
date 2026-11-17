// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.client.ZFolder;

public class ZCreateFolderEvent implements ZCreateItemEvent {

    protected ZFolder mFolder;

    public ZCreateFolderEvent(ZFolder folder) throws ServiceException {
        mFolder = folder;
    }

	public String toString() {
    	return mFolder.toString();
    }
}
