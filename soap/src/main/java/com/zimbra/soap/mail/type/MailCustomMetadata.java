// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.CustomMetadataInterface;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_METADATA)
public class MailCustomMetadata
extends MailKeyValuePairs
implements CustomMetadataInterface {

    // Normally present but sometimes an empty element is created to show that
    // CustomMetadata info is present but there are no sections to report on.
    /**
     * @zm-api-field-tag section
     * @zm-api-field-description Section.
     * <br />
     * Normally present.  If absent this indicates that CustomMetadata info is present but there are no sections to
     * report on.
     */
    @XmlAttribute(name=MailConstants.A_SECTION /* section */, required=false)
    private String section;

    public MailCustomMetadata() {
    }

    @Override
    public void setSection(String section) { this.section = section; }
    @Override
    public String getSection() { return section; }

    public static List <MailCustomMetadata> fromInterfaces(Iterable <CustomMetadataInterface> params) {
        if (params == null) {
            return null;
        }
        final List <MailCustomMetadata> newList = Lists.newArrayList();
        for (final CustomMetadataInterface param : params) {
            newList.add((MailCustomMetadata) param);
        }
        return newList;
    }

    public static List <CustomMetadataInterface> toInterfaces(Iterable <MailCustomMetadata> params) {
        if (params == null) {
            return null;
        }
        final List <CustomMetadataInterface> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return Collections.unmodifiableList(newList);
    }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("section", section);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
