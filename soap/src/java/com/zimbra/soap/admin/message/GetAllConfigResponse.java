// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;

@XmlRootElement(name=AdminConstants.E_GET_ALL_CONFIG_RESPONSE)
public class GetAllConfigResponse extends AdminAttrsImpl {

    public GetAllConfigResponse() {
    }
}
