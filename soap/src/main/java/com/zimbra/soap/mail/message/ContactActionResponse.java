// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.FolderActionResult;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_CONTACT_ACTION_RESPONSE)
public class ContactActionResponse {

    /**
     * @zm-api-field-description Action result
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_ACTION /* action */, required=true)
    private final FolderActionResult action;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ContactActionResponse() {
        this((FolderActionResult) null);
    }

    public ContactActionResponse(FolderActionResult action) {
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
