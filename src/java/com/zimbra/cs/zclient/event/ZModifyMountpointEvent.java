/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.zclient.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.mail.MailService;
import com.zimbra.cs.zclient.ZSoapSB;
import com.zimbra.soap.Element;

public class ZModifyMountpointEvent extends ZModifyFolderEvent {


    public ZModifyMountpointEvent(Element e) throws ServiceException {
        super(e);
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new name or defaultValue if unchanged
     */
    public String getOwnerDisplayName(String defaultValue) {
        return mFolderEl.getAttribute(MailService.A_OWNER_NAME, defaultValue);
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new name or defaultValue if unchanged
     */
    public String getRemoteId(String defaultValue) {
        return mFolderEl.getAttribute(MailService.A_REMOTE_ID, defaultValue);
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new name or defaultValue if unchanged
     */
    public String getOwnerId(String defaultValue) {
        return mFolderEl.getAttribute(MailService.A_ZIMBRA_ID, defaultValue);
    }
    
    public String toString() {
        try {
            ZSoapSB sb = new ZSoapSB();
            sb.beginStruct();
            toStringCommon(sb);
            if (getOwnerId(null) != null) sb.add("ownerId", getOwnerId(null));
            if (getOwnerDisplayName(null) != null) sb.add("ownerDisplayName", getOwnerDisplayName(null));
            if (getRemoteId(null) != null) sb.add("remoteId", getRemoteId(null));
            sb.endStruct();
            return sb.toString();
        } catch (ServiceException se) {
            return "";
        }
    }
}
