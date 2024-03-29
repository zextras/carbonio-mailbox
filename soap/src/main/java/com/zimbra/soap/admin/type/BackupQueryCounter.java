// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.BackupConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class BackupQueryCounter {

    /**
     * @zm-api-field-tag counter-name
     * @zm-api-field-description Counter name
     */
    @XmlAttribute(name=BackupConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-tag counter-unit
     * @zm-api-field-description Counter unit
     */
    @XmlAttribute(name=BackupConstants.A_COUNTER_UNIT /* unit */, required=true)
    private final String counterUnit;

    /**
     * @zm-api-field-tag counter-value
     * @zm-api-field-description Counter value
     */
    @XmlAttribute(name=BackupConstants.A_COUNTER_SUM /* sum */, required=true)
    private final Long counterSum;

    /**
     * @zm-api-field-tag num-samples-or-data-points
     * @zm-api-field-description Number of samples or data points
     */
    @XmlAttribute(name=BackupConstants.A_COUNTER_NUM_SAMPLES /* numSamples */, required=true)
    private final Long counterNumSamples;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private BackupQueryCounter() {
        this(null, null, null, null);
    }

    public BackupQueryCounter(String name, String counterUnit,
                            Long counterSum, Long counterNumSamples) {
        this.name = name;
        this.counterUnit = counterUnit;
        this.counterSum = counterSum;
        this.counterNumSamples = counterNumSamples;
    }

    public String getName() { return name; }
    public String getCounterUnit() { return counterUnit; }
    public Long getCounterSum() { return counterSum; }
    public Long getCounterNumSamples() { return counterNumSamples; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("counterUnit", counterUnit)
            .add("counterSum", counterSum)
            .add("counterNumSamples", counterNumSamples);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
