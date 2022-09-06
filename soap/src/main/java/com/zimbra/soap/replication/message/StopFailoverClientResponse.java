// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.replication.message;

import com.zimbra.common.soap.ReplicationConstants;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = ReplicationConstants.E_STOP_HA_CLIENT_RESPONSE)
public class StopFailoverClientResponse {}
