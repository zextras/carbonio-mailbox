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
import com.zimbra.soap.mail.type.TagSpec;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Create a tag
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_CREATE_TAG_REQUEST)
public class CreateTagRequest {

    /**
     * @zm-api-field-description Tag specification
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_TAG /* tag */, required=false)
    private TagSpec tag;

    public CreateTagRequest() {
    }

    public void setTag(TagSpec tag) { this.tag = tag; }
    public TagSpec getTag() { return tag; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tag", tag)
            .toString();
    }
}
