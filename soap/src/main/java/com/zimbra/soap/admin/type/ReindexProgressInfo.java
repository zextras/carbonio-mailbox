// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ReindexProgressInfo {

    /**
     * @zm-api-field-tag succeeded
     * @zm-api-field-description Number of reindexes that succeeded
     */
    @XmlAttribute(name=AdminConstants.A_NUM_SUCCEEDED /* numSucceeded */, required=true)
    private final int numSucceeded;

    /**
     * @zm-api-field-tag failed
     * @zm-api-field-description Number of reindexes that failed
     */
    @XmlAttribute(name=AdminConstants.A_NUM_FAILED /* numFailed */, required=true)
    private final int numFailed;

    /**
     * @zm-api-field-tag remaining
     * @zm-api-field-description Number of reindexes that remaining
     */
    @XmlAttribute(name=AdminConstants.A_NUM_REMAINING /* numRemaining */, required=true)
    private final int numRemaining;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ReindexProgressInfo() {
        this(-1, -1, -1);
    }

    public ReindexProgressInfo(
            int numSucceeded, int numFailed, int numRemaining) {
        this.numSucceeded = numSucceeded;
        this.numFailed = numFailed;
        this.numRemaining = numRemaining;
    }

    public int getNumSucceeded() { return numSucceeded; }
    public int getNumFailed() { return numFailed; }
    public int getNumRemaining() { return numRemaining; }
}
