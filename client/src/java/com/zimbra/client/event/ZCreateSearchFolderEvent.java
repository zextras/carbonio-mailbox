// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.client.ZItem;
import com.zimbra.client.ZSearchFolder;

public class ZCreateSearchFolderEvent implements ZCreateItemEvent {

    protected ZSearchFolder mSearchFolder;

    public ZCreateSearchFolderEvent(ZSearchFolder searchFolder) throws ServiceException {
        mSearchFolder = searchFolder;
    }

    /**
     * @return id of created search folder.
     * @throws com.zimbra.common.service.ServiceException
     */
    public String getId() throws ServiceException {
        return mSearchFolder.getId();
    }

    public ZItem getItem() throws ServiceException {
        return mSearchFolder;
    }

    public ZSearchFolder getSearchFolder() {
        return mSearchFolder;
    }
    
    public String toString() {
    	return mSearchFolder.toString();
    }
}
