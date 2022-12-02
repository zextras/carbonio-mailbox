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
import com.zimbra.soap.mail.type.MsgPartIds;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Remove attachments from a message body
 * <br />
 * <b>NOTE</b> that this operation is effectively a create and a delete, and thus the message's item ID will change
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_REMOVE_ATTACHMENTS_REQUEST)
public class RemoveAttachmentsRequest {

    /**
     * @zm-api-field-description Specification of parts to remove
     */
    @XmlElement(name=MailConstants.E_MSG /* m */, required=true)
    private final MsgPartIds msg;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private RemoveAttachmentsRequest() {
        this((MsgPartIds) null);
    }

    public RemoveAttachmentsRequest(MsgPartIds msg) {
        this.msg = msg;
    }

    public MsgPartIds getMsg() { return msg; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("msg", msg);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
