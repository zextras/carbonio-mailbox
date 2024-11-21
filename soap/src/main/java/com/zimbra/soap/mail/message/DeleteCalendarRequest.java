// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import com.zimbra.soap.mail.type.FolderActionSelector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: This class is extracted from FolderAction. Only calendar ids are needed for this request. Remove unnecessary fields.
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_DELETE_CALENDAR_REQUEST)
public class DeleteCalendarRequest {

    /**
     * @zm-api-field-description Select action to perform on folder
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_ACTION /* action */, required=true)
    private final FolderActionSelector action;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    public DeleteCalendarRequest() {
        this(null);
    }

    public DeleteCalendarRequest(FolderActionSelector action) {
        this.action = action;
    }

    public FolderActionSelector getAction() { return action; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("action", action)
                .toString();
    }
}
