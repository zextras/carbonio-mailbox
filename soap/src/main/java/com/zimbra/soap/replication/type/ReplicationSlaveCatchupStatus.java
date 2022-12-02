// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.replication.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.ReplicationConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ReplicationSlaveCatchupStatus {

    /**
     * @zm-api-field-tag num-pending-redo-operations
     * @zm-api-field-description Number of pending redo operations
     */
    @XmlAttribute(name=ReplicationConstants.A_REMAINING_OPS /* remainingOps */, required=true)
    private final int remaingingOps;

    /**
     * @zm-api-field-tag num-remaining-files
     * @zm-api-field-description Number of remaining files
     */
    @XmlAttribute(name=ReplicationConstants.A_REMAINING_FILES /* remainingFiles */, required=true)
    private final int remaingingFiles;

    /**
     * @zm-api-field-tag num-remaining-bytes
     * @zm-api-field-description Number of remaining bytes
     */
    @XmlAttribute(name=ReplicationConstants.A_REMAINING_BYTES /* remainingBytes */, required=true)
    private final long remaingingBytes;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ReplicationSlaveCatchupStatus() {
        this(-1, -1, -1L);
    }

    public ReplicationSlaveCatchupStatus(int remaingingOps, int remaingingFiles, long remaingingBytes) {
        this.remaingingOps = remaingingOps;
        this.remaingingFiles = remaingingFiles;
        this.remaingingBytes = remaingingBytes;
    }

    public int getRemaingingOps() { return remaingingOps; }
    public int getRemaingingFiles() { return remaingingFiles; }
    public long getRemaingingBytes() { return remaingingBytes; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("remaingingOps", remaingingOps)
            .add("remaingingFiles", remaingingFiles)
            .add("remaingingBytes", remaingingBytes);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
