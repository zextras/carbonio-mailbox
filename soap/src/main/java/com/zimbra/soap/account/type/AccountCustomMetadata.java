// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.Collections;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.CustomMetadataInterface;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_METADATA)
public class AccountCustomMetadata
extends AccountKeyValuePairs
implements CustomMetadataInterface {

    /**
     * @zm-api-field-tag metadata-section-key
     * @zm-api-field-description Metadata section key.
     * <br />
     * Must be no more than 36 characters long and must be in the format of <b>{namespace}:{section-name}</b>.
     * Currently the only valid namespace is "zwc".
     * <br />
     * <br />
     * Normally present but sometimes an empty element is created to show that custom metadata info is present but
     * there are no sections to report on.
     */
    @XmlAttribute(name=MailConstants.A_SECTION /* section */, required=false)
    private String section;

    public AccountCustomMetadata() {
    }

    @Override
    public void setSection(String section) { this.section = section; }
    @Override
    public String getSection() { return section; }

    public static List <AccountCustomMetadata> fromInterfaces(Iterable <CustomMetadataInterface> params) {
        if (params == null)
            return null;
        List <AccountCustomMetadata> newList = Lists.newArrayList();
        for (CustomMetadataInterface param : params) {
            newList.add((AccountCustomMetadata) param);
        }
        return newList;
    }

    public static List <CustomMetadataInterface> toInterfaces(Iterable <AccountCustomMetadata> params) {
        if (params == null)
            return null;
        List <CustomMetadataInterface> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return Collections.unmodifiableList(newList);
    }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("section", section);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
