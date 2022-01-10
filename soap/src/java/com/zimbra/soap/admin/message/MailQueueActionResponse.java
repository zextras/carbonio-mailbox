// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

// Note: soap-admin.txt claimed that response was same as for GetMailQueueResponse but that isn't the case
@XmlRootElement(name=AdminConstants.E_MAIL_QUEUE_ACTION_RESPONSE)
public class MailQueueActionResponse {
}
