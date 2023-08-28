// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.replication.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.ReplicationConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ReplicationSlaveStatus {

    /**
     * @zm-api-field-description Catchup status
     */
    @ZimbraUniqueElement
    @XmlElement(name=ReplicationConstants.E_CATCHUP_STATUS /* catchupStatus */, required=false)
    private ReplicationSlaveCatchupStatus catchupStatus;

    public ReplicationSlaveStatus() {
    }

    public void setCatchupStatus(ReplicationSlaveCatchupStatus catchupStatus) {
        this.catchupStatus = catchupStatus;
    }
    public ReplicationSlaveCatchupStatus getCatchupStatus() { return catchupStatus; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("catchupStatus", catchupStatus);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
