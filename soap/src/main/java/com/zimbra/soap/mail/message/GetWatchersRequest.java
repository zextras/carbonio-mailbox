// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.OctopusXmlConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Returns a list of items in the user's mailbox currently being watched by other users.
 */
@XmlRootElement(name=OctopusXmlConstants.E_GET_WATCHERS_REQUEST)
public class GetWatchersRequest {
}
