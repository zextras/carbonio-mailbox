// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.soap.mail.type.Msg;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Propose a new time/location.  Sent by meeting attendee to organizer.
 * <br />
 * The syntax is very similar to CreateAppointmentRequest.
 * <br />
 * <br />
 * Should include an <b>&lt;inv></b> element which encodes an iCalendar COUNTER object
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_COUNTER_APPOINTMENT_REQUEST)
public class CounterAppointmentRequest {

    /**
     * @zm-api-field-tag invite-id-of-default-invite
     * @zm-api-field-description Invite ID of default invite
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private String id;

    /**
     * @zm-api-field-tag default-invite-component-num
     * @zm-api-field-description Component number of default component
     */
    @XmlAttribute(name=MailConstants.A_CAL_COMP /* comp */, required=false)
    private Integer componentNum;

    /**
     * @zm-api-field-tag changed-seq-of-fetched-version
     * @zm-api-field-description Changed sequence of fetched version.
     * <br />
     * Used for conflict detection.  By setting this, the request indicates which version of the appointment it is
     * attempting to propose.  If the appointment was updated on the server between the fetch and modify, an
     * <b>INVITE_OUT_OF_DATE exception</b> will be thrown.
     */
    @XmlAttribute(name=MailConstants.A_MODIFIED_SEQUENCE /* ms */, required=false)
    private Integer modifiedSequence;

    /**
     * @zm-api-field-tag
     * @zm-api-field-description
     */
    @XmlAttribute(name=MailConstants.A_REVISION /* rev */, required=false)
    private Integer revision;

    /**
     * @zm-api-field-description Details of counter proposal.
     */
    @XmlElement(name=MailConstants.E_MSG /* m */, required=false)
    private Msg msg;

    public CounterAppointmentRequest() {
    }

    public static CounterAppointmentRequest createForMsgModseqRevIdCompnum(
            Msg msg, Integer modifiedSeq, Integer rev, String id, Integer compNum) {
        CounterAppointmentRequest req = new CounterAppointmentRequest();
        req.setMsg(msg);
        req.setModifiedSequence(modifiedSeq);
        req.setRevision(rev);
        req.setId(id);
        req.setComponentNum(compNum);
        return req;
    }

    public void setId(String id) { this.id = id; }
    public void setComponentNum(Integer componentNum) { this.componentNum = componentNum; }
    public void setModifiedSequence(Integer modifiedSequence) { this.modifiedSequence = modifiedSequence; }
    public void setRevision(Integer revision) { this.revision = revision; }
    public String getId() { return id; }
    public Integer getComponentNum() { return componentNum; }
    public Integer getModifiedSequence() { return modifiedSequence; }
    public Integer getRevision() { return revision; }

    public void setMsg(Msg msg) { this.msg = msg; }
    public Msg getMsg() { return msg; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("msg", msg)
            .add("id", id)
            .add("componentNum", componentNum)
            .add("modifiedSequence", modifiedSequence)
            .add("revision", revision);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
