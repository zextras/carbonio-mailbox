// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;

public class ZModifyMountpointEvent extends ZModifyFolderEvent {


    public ZModifyMountpointEvent(Element e) throws ServiceException {
        super(e);
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new name or defaultValue if unchanged
     */
    public String getOwnerDisplayName(String defaultValue) {
        return mFolderEl.getAttribute(MailConstants.A_OWNER_NAME, defaultValue);
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new name or defaultValue if unchanged
     */
    public String getRemoteId(String defaultValue) {
        return mFolderEl.getAttribute(MailConstants.A_REMOTE_ID, defaultValue);
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new name or defaultValue if unchanged
     */
    public String getOwnerId(String defaultValue) {
        return mFolderEl.getAttribute(MailConstants.A_ZIMBRA_ID, defaultValue);
    }
}
