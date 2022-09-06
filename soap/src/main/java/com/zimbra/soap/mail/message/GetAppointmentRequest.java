// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.GetCalendarItemRequestBase;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Appointment. Returns the metadata info for each Invite that makes
 *     up this appointment. <br>
 *     The content (original email) for each invite is stored within the Appointment itself in a big
 *     multipart/digest containing each invite in the appointment as a sub-mimepart it can be
 *     retreived from the content servlet:
 *     <pre>
 *     http://servername/service/content/get?id=&lt;calItemId>
 * </pre>
 *     The content for a single Invite can be requested from the content servlet (or from
 *     <b>&lt;GetMsg></b>) Individual. The client can ALSO request just the content for each
 *     individual invite using a compound item-id request:
 *     <pre>
 *     http://servername/service/content/get?id="calItemId-invite_mail_item_id"
 *     &lt;GetMsgRequest>&lt;m id="calItemId-invite_mail_item_id"
 * </pre>
 *     <b>IMPORTANT NOTE</b>: DO NOT use the raw invite-mail-item-id to fetch the content: it might
 *     work sometimes, however the invite is a standard mail-message it can be deleted by the user
 *     at any time!
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_APPOINTMENT_REQUEST)
public class GetAppointmentRequest extends GetCalendarItemRequestBase {
  public GetAppointmentRequest() {}

  public static GetAppointmentRequest createForUidInvitesContent(
      String reqUid, Boolean includeInvites, Boolean includeContent) {
    GetAppointmentRequest req = new GetAppointmentRequest();
    req.setUid(reqUid);
    req.setIncludeContent(includeContent);
    req.setIncludeInvites(includeInvites);
    return req;
  }
}
