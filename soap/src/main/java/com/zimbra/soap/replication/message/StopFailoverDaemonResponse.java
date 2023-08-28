// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.replication.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.ReplicationConstants;

@XmlRootElement(name=ReplicationConstants.E_STOP_HA_DAEMON_RESPONSE)
public class StopFailoverDaemonResponse {
}
