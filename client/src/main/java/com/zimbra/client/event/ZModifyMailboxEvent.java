// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.HeaderConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.client.ToZJSONObject;
import com.zimbra.client.ZJSONObject;
import org.json.JSONException;

public class ZModifyMailboxEvent implements ZModifyEvent, ToZJSONObject {

    protected Element mMailboxEl;

    public ZModifyMailboxEvent(Element e) {
        mMailboxEl = e;
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new size, or defaultValue if unchanged
     * @throws ServiceException on error
     */
    public long getSize(long defaultValue) throws ServiceException {
        return mMailboxEl.getAttributeLong(MailConstants.A_SIZE, defaultValue);
    }

    public String getOwner(String defaultId) {
        return mMailboxEl.getAttribute(HeaderConstants.A_ACCOUNT_ID, defaultId);
    }

    public ZJSONObject toZJSONObject() throws JSONException {
        try {
            ZJSONObject zjo = new ZJSONObject();
            if (getSize(-1) != -1) zjo.put("size", getSize(-1));
            if (getOwner(null) != null) zjo.put("owner", getOwner(null));
            return zjo;
        } catch (ServiceException se) {
            throw new JSONException(se);
        }
    }

    public String toString() {
        return String.format("[ZModifyMailboxEvent]"); // TODO
    }

    public String dump() {
        return ZJSONObject.toString(this);
    }
}
