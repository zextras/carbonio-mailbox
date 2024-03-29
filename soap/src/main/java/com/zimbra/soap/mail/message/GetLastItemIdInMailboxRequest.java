// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Returns the last ID assigned to an item successfully created in the mailbox
 */
@XmlRootElement(name=MailConstants.E_GET_LAST_ITEM_ID_IN_MAILBOX_REQUEST)
public class GetLastItemIdInMailboxRequest {

    public GetLastItemIdInMailboxRequest() {}

}
