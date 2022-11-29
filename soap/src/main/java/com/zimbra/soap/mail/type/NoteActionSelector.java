// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class NoteActionSelector extends ActionSelector {

    /**
     * @zm-api-field-tag content
     * @zm-api-field-description Content
     */
    @XmlAttribute(name=MailConstants.E_CONTENT /* content */, required=false)
    private String content;

    /**
     * @zm-api-field-tag bounds-x,y[width,height]
     * @zm-api-field-description Bounds - <b>x,y[width,height]</b> where x,y,width and height are all integers
     */
    @XmlAttribute(name=MailConstants.A_BOUNDS /* pos */, required=false)
    private String bounds;

    public NoteActionSelector() {
    }

    public void setContent(String content) { this.content = content; }
    public void setBounds(String bounds) { this.bounds = bounds; }
    public String getContent() { return content; }
    public String getBounds() { return bounds; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("content", content)
            .add("bounds", bounds);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
