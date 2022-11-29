// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_ADD_APPOINTMENT_INVITE_RESPONSE)
public class AddAppointmentInviteResponse {

    /**
     * @zm-api-field-tag calendar-item-id
     * @zm-api-field-description Calendar item ID
     */
    @XmlAttribute(name=MailConstants.A_CAL_ID /* calItemId */, required=false)
    private Integer calItemId;

    /**
     * @zm-api-field-tag added-invite-id
     * @zm-api-field-description Invite ID of the added invite
     */
    @XmlAttribute(name=MailConstants.A_CAL_INV_ID /* invId */, required=false)
    private Integer invId;

    /**
     * @zm-api-field-tag component-number
     * @zm-api-field-description Component number of the added invite
     */
    @XmlAttribute(name=MailConstants.A_CAL_COMPONENT_NUM /* compNum */, required=false)
    private Integer componentNum;

    public AddAppointmentInviteResponse() {
    }

    public void setCalItemId(Integer calItemId) { this.calItemId = calItemId; }
    public void setInvId(Integer invId) { this.invId = invId; }
    public void setComponentNum(Integer componentNum) {
        this.componentNum = componentNum;
    }
    public Integer getCalItemId() { return calItemId; }
    public Integer getInvId() { return invId; }
    public Integer getComponentNum() { return componentNum; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("calItemId", calItemId)
            .add("invId", invId)
            .add("componentNum", componentNum);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
