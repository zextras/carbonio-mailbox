// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.SyncAdminConstants;

@XmlRootElement(name=SyncAdminConstants.E_REMOVE_STALE_DEVICE_METADATA_RESPONSE)
public class RemoveStaleDeviceMetadataResponse {

    public RemoveStaleDeviceMetadataResponse() {}

}
