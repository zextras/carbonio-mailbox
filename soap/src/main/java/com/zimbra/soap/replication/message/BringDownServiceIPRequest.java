// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.replication.message;

import com.zimbra.common.soap.ReplicationConstants;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Bring down the configured service IP address
 */
@XmlRootElement(name = ReplicationConstants.E_BRING_DOWN_SERVICE_IP_REQUEST)
public class BringDownServiceIPRequest {}
