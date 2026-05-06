// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.TagInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_CREATE_TAG_RESPONSE)
public class CreateTagResponse {

    /**
     * @zm-api-field-description Information about the newly created tag
     */
    @XmlElement(name=MailConstants.E_TAG, required=false)
    private TagInfo tag;

    public CreateTagResponse() {
    }

    public void setTag(TagInfo tag) { this.tag = tag; }
    public TagInfo getTag() { return tag; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tag", tag)
            .toString();
    }
}
