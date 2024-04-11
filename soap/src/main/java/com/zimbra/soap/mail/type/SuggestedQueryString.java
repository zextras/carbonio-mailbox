// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.soap.type.BaseQueryInfo;

@XmlAccessorType(XmlAccessType.NONE)
public class SuggestedQueryString implements BaseQueryInfo {

    /**
     * @zm-api-field-tag suggested-query-string
     * @zm-api-field-description Suggested query string
     */
    @XmlValue
    private String suggestedQueryString;

    /**
     * no-argument constructor wanted by JAXB
     */
    private SuggestedQueryString() {
        this(null);
    }

    private SuggestedQueryString(String suggestedQueryString) {
        this.suggestedQueryString = suggestedQueryString;
    }

    public static SuggestedQueryString createForSuggestedQueryString(String suggestedQueryString) {
        return new SuggestedQueryString(suggestedQueryString);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("suggestedQueryString", suggestedQueryString);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
