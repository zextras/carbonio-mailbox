// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.header;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.HeaderConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class HeaderNotifyInfo {

    /**
     * @zm-api-field-tag sequence-num
     * @zm-api-field-description Sequence number for the highest known notification ID
     */
    @XmlAttribute(name=HeaderConstants.A_SEQNO /* seq */, required=false)
    private Integer sequenceNum;

    public HeaderNotifyInfo() {
    }

    public void setSequenceNum(Integer sequenceNum) { this.sequenceNum = sequenceNum; }
    public Integer getSequenceNum() { return sequenceNum; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("sequenceNum", sequenceNum);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
