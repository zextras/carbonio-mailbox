// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class RawInvite {

    /**
     * @zm-api-field-tag UID
     * @zm-api-field-description UID
     */
    @XmlAttribute(name=MailConstants.A_UID /* uid */, required=false)
    private String uid;

    /**
     * @zm-api-field-tag summary
     * @zm-api-field-description summary
     */
    @XmlAttribute(name=MailConstants.A_SUMMARY /* summary */, required=false)
    private String summary;

    /**
     * @zm-api-field-tag raw-icalendar
     * @zm-api-field-description Raw iCalendar data
     */
    @XmlValue
    private String content;

    public RawInvite() {
    }

    public void setUid(String uid) { this.uid = uid; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setContent(String content) { this.content = content; }
    public String getUid() { return uid; }
    public String getSummary() { return summary; }
    public String getContent() { return content; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("uid", uid)
            .add("summary", summary)
            .add("content", content);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
