// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.replication.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.ReplicationConstants;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Stop Failover client on replication slave
 */
@XmlRootElement(name=ReplicationConstants.E_STOP_HA_CLIENT_REQUEST)
public class StopFailoverClientRequest {
}
