// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.HsmConstants;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Starts the HSM process, which moves blobs for older messages to the
 *     current secondary message volume. This request is asynchronous. The progress of the last HSM
 *     process can be monitored with <b>GetHsmStatusRequest</b>. The HSM policy is read from the
 *     zimbraHsmPolicy LDAP attribute.
 */
@XmlRootElement(name = HsmConstants.E_HSM_REQUEST)
public class HsmRequest {}
