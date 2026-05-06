// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlValue;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class DistributionListSelector {
    @XmlEnum
    public enum DistributionListBy {
        // case must match protocol
        id, name;

        public static DistributionListBy fromString(String s) throws ServiceException {
            try {
                return DistributionListBy.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ServiceException.INVALID_REQUEST("unknown key: "+s, e);
            }
        }

        public com.zimbra.common.account.Key.DistributionListBy toKeyDistributionListBy()
        throws ServiceException {
            return com.zimbra.common.account.Key.DistributionListBy.fromString(this.name());
        }
    }

    /**
     * @zm-api-field-tag dl-selector-by
     * @zm-api-field-description Select the meaning of <b>{dl-selector-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY)
    private final DistributionListBy dlBy;

    /**
     * @zm-api-field-tag dl-selector-key
     * @zm-api-field-description The key used to identify the distribution list.
     * Meaning determined by <b>{dl-selector-by}</b>
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

    public String getKey() {
        return key;
    }

    public DistributionListBy getBy() {
        return dlBy;
    }

    public static DistributionListSelector fromId(String id) {
        return new DistributionListSelector(DistributionListBy.id, id);
    }

    public static DistributionListSelector fromName(String name) {
        return new DistributionListSelector(DistributionListBy.name, name);
    }
}
