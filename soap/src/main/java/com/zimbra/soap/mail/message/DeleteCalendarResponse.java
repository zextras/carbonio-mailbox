// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import com.zimbra.soap.mail.type.FolderActionResult;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = MailConstants.E_DELETE_CALENDAR_RESPONSE)
public class DeleteCalendarResponse {

    /**
     * @zm-api-field-description Folder action result
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_ACTION /* action */, required=true)
    private final FolderActionResult action;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DeleteCalendarResponse() {
        this(null);
    }

    public DeleteCalendarResponse(FolderActionResult action) {
        this.action = action;
    }

    public FolderActionResult getAction() { return action; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("action", action)
                .toString();
    }
}
