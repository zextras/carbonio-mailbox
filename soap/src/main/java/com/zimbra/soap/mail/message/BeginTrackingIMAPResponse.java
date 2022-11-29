// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

@XmlRootElement(name=MailConstants.E_BEGIN_TRACKING_IMAP_RESPONSE)
public class BeginTrackingIMAPResponse {
    public BeginTrackingIMAPResponse() {};
}
