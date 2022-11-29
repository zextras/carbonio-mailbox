// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.TagInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_TAG_RESPONSE)
public class GetTagResponse {

    /**
     * @zm-api-field-description Information about tags
     */
    @XmlElement(name=MailConstants.E_TAG, required=false)
    private List<TagInfo> tags = Lists.newArrayList();

    public GetTagResponse() {
    }

    public void setTags(Iterable <TagInfo> tags) {
        this.tags.clear();
        if (tags != null) {
            Iterables.addAll(this.tags,tags);
        }
    }

    public GetTagResponse addTag(TagInfo tag) {
        this.tags.add(tag);
        return this;
    }

    public List<TagInfo> getTags() {
        return Collections.unmodifiableList(tags);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tags", tags)
            .toString();
    }
}
