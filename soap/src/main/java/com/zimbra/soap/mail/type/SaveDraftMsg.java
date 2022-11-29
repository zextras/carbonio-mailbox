// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class SaveDraftMsg extends Msg {

    /**
     * @zm-api-field-tag existing-draft-id
     * @zm-api-field-description Existing draft ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private Integer id;

    /**
     * @zm-api-field-tag draft-acct-id
     * @zm-api-field-description Account ID the draft is for
     */
    @XmlAttribute(name=MailConstants.A_FOR_ACCOUNT /* forAcct */, required=false)
    private String draftAccountId;

    /**
     * @zm-api-field-tag tags
     * @zm-api-field-description Tags - Comma separated list of integers.  DEPRECATED - use "tn" instead
     */
    @Deprecated
    @XmlAttribute(name=MailConstants.A_TAGS /* t */, required=false)
    private String tags;

    /**
     * @zm-api-field-tag tag-names
     * @zm-api-field-description Comma-separated list of tag names
     */
    @XmlAttribute(name=MailConstants.A_TAG_NAMES /* tn */, required=false)
    private String tagNames;

    /**
     * @zm-api-field-tag rgb-color
     * @zm-api-field-description RGB color in format #rrggbb where r,g and b are hex digits
     */
    @XmlAttribute(name=MailConstants.A_RGB /* rgb */, required=false)
    private String rgb;

    /**
     * @zm-api-field-tag color
     * @zm-api-field-description color numeric; range 0-127; defaults to 0 if not present; client can display only 0-7
     */
    @XmlAttribute(name=MailConstants.A_COLOR /* color */, required=false)
    private Byte color;

    /**
     * @zm-api-field-tag auto-send-time-millis
     * @zm-api-field-description Auto send time in milliseconds since the epoch
     */
    @XmlAttribute(name=MailConstants.A_AUTO_SEND_TIME /* autoSendTime */, required=false)
    private Long autoSendTime;

    public SaveDraftMsg() {
    }

    public void setId(Integer id) { this.id = id; }
    public void setDraftAccountId(String draftAccountId) {
        this.draftAccountId = draftAccountId;
    }
    @Deprecated
    public void setTags(String tags) { this.tags = tags; }
    public void setTagNames(String tagNames) { this.tagNames = tagNames; }
    public void setRgb(String rgb) { this.rgb = rgb; }
    public void setColor(Byte color) { this.color = color; }
    public void setAutoSendTime(Long autoSendTime) {
        this.autoSendTime = autoSendTime;
    }

    public Integer getId() { return id; }
    public String getDraftAccountId() { return draftAccountId; }
    @Deprecated
    public String getTags() { return tags; }
    public String getTagNames() { return tagNames; }
    public String getRgb() { return rgb; }
    public Byte getColor() { return color; }
    public Long getAutoSendTime() { return autoSendTime; }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("id", id)
            .add("draftAccountId", draftAccountId)
            .add("tags", tags)
            .add("tagNames", tagNames)
            .add("rgb", rgb)
            .add("color", color)
            .add("autoSendTime", autoSendTime);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
