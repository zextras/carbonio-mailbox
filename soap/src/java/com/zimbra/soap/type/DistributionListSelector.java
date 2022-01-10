// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.DistributionListBy;

@XmlAccessorType(XmlAccessType.NONE)
public class DistributionListSelector {

    /**
     * @zm-api-field-tag dl-selector-by
     * @zm-api-field-description Select the meaning of <b>{dl-selector-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY, required=true)
    private final DistributionListBy dlBy;

    /**
     * @zm-api-field-tag dl-selector-key
     * @zm-api-field-description The key used to identify the account. Meaning determined by <b>{dl-selector-by}</b>
     */
    @XmlValue
    private final String key;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DistributionListSelector() {
        this.dlBy = null;
        this.key = null;
    }

    public DistributionListSelector(DistributionListBy by, String key) {
        this.dlBy = by;
        this.key = key;
    }

    public String getKey() { return key; }

    public DistributionListBy getBy() { return dlBy; }

    public static DistributionListSelector fromId(String id) {
        return new DistributionListSelector(DistributionListBy.id, id);
    }

    public static DistributionListSelector fromName(String name) {
        return new DistributionListSelector(DistributionListBy.name, name);
    }
}
