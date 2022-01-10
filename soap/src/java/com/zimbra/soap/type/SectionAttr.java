// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class SectionAttr {

    /**
     * @zm-api-field-tag metadata-section-key
     * @zm-api-field-description Metadata section key
     */
    @XmlAttribute(name=MailConstants.A_SECTION /* section */, required=true)
    private final String section;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private SectionAttr() {
        this((String) null);
    }

    public SectionAttr(String section) {
        this.section = section;
    }

    public String getSection() { return section; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("section", section);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
