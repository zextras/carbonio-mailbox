// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class CalReply extends RecurIdInfo {

    /**
     * @zm-api-field-tag attendee-who-replied
     * @zm-api-field-description Address of attendee who replied
     */
    @XmlAttribute(name=MailConstants.A_CAL_ATTENDEE /* at */, required=true)
    private final String attendee;

    /**
     * @zm-api-field-tag sent-by
     * @zm-api-field-description SENT-BY
     */
    @XmlAttribute(name=MailConstants.A_CAL_SENTBY /* sentBy */, required=false)
    private final String sentBy;

    /**
     * @zm-api-field-tag participation-status
     * @zm-api-field-description iCalendar PTST (Participation status)
     * <br />
     * Valid values: <b>NE|AC|TE|DE|DG|CO|IN|WE|DF</b>
     * <br />
     * Meanings:
     * <br />
     * "NE"eds-action, "TE"ntative, "AC"cept, "DE"clined, "DG" (delegated), "CO"mpleted (todo), "IN"-process (todo),
     * "WA"iting (custom value only for todo), "DF" (deferred; custom value only for todo)
     */
    @XmlAttribute(name=MailConstants.A_CAL_PARTSTAT /* ptst */, required=false)
    private final String partStat;

    /**
     * @zm-api-field-tag sequence
     * @zm-api-field-description Sequence
     */
    @XmlAttribute(name=MailConstants.A_SEQ /* seq */, required=true)
    private final int sequence;

    /**
     * @zm-api-field-tag reply-timestamp
     * @zm-api-field-description Timestamp of reply
     */
    @XmlAttribute(name=MailConstants.A_DATE /* d */, required=true)
    private final int date;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CalReply() {
        this(null, null, null, -1, -1);
    }

    public CalReply(String attendee, String sentBy, String partStat, int sequence, int date) {
        this.attendee = attendee;
        this.sentBy = sentBy;
        this.partStat = partStat;
        this.sequence = sequence;
        this.date = date;
    }

    public String getAttendee() { return attendee; }
    public String getSentBy() { return sentBy; }
    public String getPartStat() { return partStat; }
    public int getSequence() { return sequence; }
    public int getDate() { return date; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("attendee", attendee)
            .add("sentBy", sentBy)
            .add("partStat", partStat)
            .add("sequence", sequence)
            .add("date", date);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
