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
public class DispositionAndText {

    /**
     * @zm-api-field-tag disposition
     * @zm-api-field-description Disposition.  Sections of text that are identical to both versions are indicated with
     * <b>disp="common"</b>.  For each conflict the chunk will show <b>disp="first"</b> or <b>disp="second"</b>
     */
    @XmlAttribute(name=MailConstants.A_DISP /* disp */, required=false)
    private String disposition;

    /**
     * @zm-api-field-tag text
     * @zm-api-field-description Text
     */
    @XmlValue
    private String text;

    public DispositionAndText() {
    }

    public DispositionAndText(String disp, String txt) {
        setDisposition(disp);
        setText(txt);
    }

    public static DispositionAndText create(String disp, String txt) {
        return new DispositionAndText(disp, txt);
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }
    public void setText(String text) { this.text = text; }
    public String getDisposition() { return disposition; }
    public String getText() { return text; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("disposition", disposition)
            .add("text", text);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
