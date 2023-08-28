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
import com.zimbra.soap.type.Id;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_ADD_COMMENT_RESPONSE)
public class AddCommentResponse {

    /**
     * @zm-api-field-tag comment-item-id
     * @zm-api-field-description Item ID for the comment
     */
    @XmlElement(name=MailConstants.E_COMMENT /* comment */, required=true)
    private final Id comment;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AddCommentResponse() {
        this((Id) null);
    }

    public AddCommentResponse(Id comment) {
        this.comment = comment;
    }

    public Id getComment() { return comment; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("comment", comment);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
