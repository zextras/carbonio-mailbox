// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.SetCalendarItemInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Add an invite to an appointment. <br>
 *     The invite corresponds to a VEVENT component. Based on the UID specified (required), a new
 *     appointment is created in the default calendar if necessary. If an appointment with the same
 *     UID exists, the appointment is updated with the new invite only if the invite is not
 *     outdated, according to the iCalendar sequencing rule (based on SEQUENCE, RECURRENCE-ID and
 *     DTSTAMP).
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_ADD_APPOINTMENT_INVITE_REQUEST)
public class AddAppointmentInviteRequest extends SetCalendarItemInfo {}
