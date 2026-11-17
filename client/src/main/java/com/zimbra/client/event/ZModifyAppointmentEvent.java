// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.client.ZAppointment;
import com.zimbra.client.ZItem;

public class ZModifyAppointmentEvent implements ZModifyItemEvent, ZModifyItemFolderEvent {

    protected Element mApptEl;

    public ZModifyAppointmentEvent(Element e) throws ServiceException {
        mApptEl = e;
    }

    /**
     * @return id
     * @throws com.zimbra.common.service.ServiceException on error
     */
    public String getId() throws ServiceException {
        return mApptEl.getAttribute(MailConstants.A_ID);
    }

    public String toString() {
        try {
            return String.format("[ZModifyAppointmentEvent %s]", getId());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    public ZItem getItem() throws ServiceException {
        return new ZAppointment(mApptEl);
    }

	public String getFolderId(String defaultValue) {
        return mApptEl.getAttribute(MailConstants.A_FOLDER, defaultValue);
    }

}
