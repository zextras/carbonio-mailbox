// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import java.util.HashMap;
import java.util.Map;


import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.KeyValuePair;
import com.zimbra.common.soap.MailConstants;

public class ZContactEvent {

    protected Element mContactEl;

    public ZContactEvent(Element e) throws ServiceException {
        mContactEl = e;
    }

    /**
     * @return id
     * @throws com.zimbra.common.service.ServiceException on error
     */
    public String getId() throws ServiceException {
        return mContactEl.getAttribute(MailConstants.A_ID);
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new flags or default value if flags didn't change
     */
    public String getFlags(String defaultValue) {
        return mContactEl.getAttribute(MailConstants.A_FLAGS, defaultValue);
    }

    /**
     * @param defaultValue value to return if unchanged
     * @return new tags or default value if tags didn't change
     */
    public String getTagIds(String defaultValue) {
        return mContactEl.getAttribute(MailConstants.A_TAGS, defaultValue);
    }

    public String getFolderId(String defaultValue) {
        return mContactEl.getAttribute(MailConstants.A_FOLDER, defaultValue);
    }

    public long getMetaDataChangedDate(long defaultValue) throws ServiceException {
        return mContactEl.getAttributeLong(MailConstants.A_CHANGE_DATE, defaultValue);
    }

    public long getDate(long defaultValue) throws ServiceException {
        return mContactEl.getAttributeLong(MailConstants.A_DATE, defaultValue);
    }
    
    public String getFileAsStr(String defaultValue) {
        return mContactEl.getAttribute(MailConstants.A_FILE_AS_STR, defaultValue);
    }

    public String getRevision(String defaultValue) {
        return mContactEl.getAttribute(MailConstants.A_REVISION, defaultValue);
    }

    public String getEmail(String defaultValue) {
        return mContactEl.getAttribute(ContactConstants.A_email, defaultValue);
    }

    public Map<String, String> getAttrs(Map<String, String> defaultValue) throws ServiceException {
    	Map<String, String> attrs = null;
        for (KeyValuePair pair : mContactEl.listKeyValuePairs(MailConstants.E_ATTRIBUTE, MailConstants.A_ATTRIBUTE_NAME)) {
            if (attrs == null) attrs = new HashMap<>();
            attrs.put(pair.getKey(), pair.getValue());

        }
        return attrs != null ? attrs : defaultValue;
    }

    public String toString() {
        try {
            return String.format("[ZContactEvent %s]", getId());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
