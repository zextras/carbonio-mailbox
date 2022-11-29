// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.sync.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.SyncConstants;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get status for devices
 */
@XmlRootElement(name=SyncConstants.E_GET_DEVICE_STATUS_REQUEST)
public class GetDeviceStatusRequest {
}
