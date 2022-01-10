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
import com.zimbra.soap.mail.type.BrowseData;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_BROWSE_RESPONSE)
public class BrowseResponse {

    /**
     * @zm-api-field-description Browse data
     */
    @XmlElement(name=MailConstants.E_BROWSE_DATA, required=false)
    private List<BrowseData> browseDatas = Lists.newArrayList();

    public BrowseResponse() {
    }

    public void setBrowseDatas(Iterable <BrowseData> browseDatas) {
        this.browseDatas.clear();
        if (browseDatas != null) {
            Iterables.addAll(this.browseDatas,browseDatas);
        }
    }

    public BrowseResponse addBrowseData(BrowseData browseData) {
        this.browseDatas.add(browseData);
        return this;
    }

    public List<BrowseData> getBrowseDatas() {
        return Collections.unmodifiableList(browseDatas);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("browseDatas", browseDatas)
            .toString();
    }
}
