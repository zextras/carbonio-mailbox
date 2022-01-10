// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.replication.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.ReplicationConstants;
import com.zimbra.soap.replication.type.ReplicationMasterStatus;
import com.zimbra.soap.replication.type.ReplicationSlaveStatus;
import com.zimbra.soap.type.ZmBoolean;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=ReplicationConstants.E_REPLICATION_STATUS_RESPONSE)
public class ReplicationStatusResponse {

    /**
     * @zm-api-field-tag replication-enabled-flag
     * @zm-api-field-description Flags whether replication is enabled
     */
    @XmlAttribute(name=ReplicationConstants.A_REPLICATION_ENABLED /* replicationEnabled */, required=true)
    private final ZmBoolean replicationEnabled;

    /**
     * @zm-api-field-tag current-role-master|slave
     * @zm-api-field-description Current role - <b>master|slave</b>
     */
    @XmlAttribute(name=ReplicationConstants.A_CURRENT_ROLE /* currentRole */, required=false)
    private String currentRole;

    /**
     * @zm-api-field-tag original-role-master|slave
     * @zm-api-field-description Original role - <b>master|slave</b>
     */
    @XmlAttribute(name=ReplicationConstants.A_ORIGINAL_ROLE /* originalRole */, required=false)
    private String originalRole;

    // Only one of masterStatus and slaveStatus can be present

    /**
     * @zm-api-field-description Master replication status.  Only one of masterStatus and slaveStatus can be present
     */
    @ZimbraUniqueElement
    @XmlElement(name=ReplicationConstants.E_MASTER_STATUS /* masterStatus */, required=false)
    private ReplicationMasterStatus masterStatus;

    /**
     * @zm-api-field-description Slave replication status
     */
    @ZimbraUniqueElement
    @XmlElement(name=ReplicationConstants.E_SLAVE_STATUS /* slaveStatus */, required=false)
    private ReplicationSlaveStatus slaveStatus;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ReplicationStatusResponse() {
        this(false);
    }

    public ReplicationStatusResponse(boolean replicationEnabled) {
        this.replicationEnabled = ZmBoolean.fromBool(replicationEnabled);
    }

    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }
    public void setOriginalRole(String originalRole) { this.originalRole = originalRole; }
    public void setMasterStatus(ReplicationMasterStatus masterStatus) { this.masterStatus = masterStatus; }
    public void setSlaveStatus(ReplicationSlaveStatus slaveStatus) { this.slaveStatus = slaveStatus; }
    public boolean getReplicationEnabled() { return ZmBoolean.toBool(replicationEnabled); }
    public String getCurrentRole() { return currentRole; }
    public String getOriginalRole() { return originalRole; }
    public ReplicationMasterStatus getMasterStatus() { return masterStatus; }
    public ReplicationSlaveStatus getSlaveStatus() { return slaveStatus; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("replicationEnabled", replicationEnabled)
            .add("currentRole", currentRole)
            .add("originalRole", originalRole)
            .add("masterStatus", masterStatus)
            .add("slaveStatus", slaveStatus);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
