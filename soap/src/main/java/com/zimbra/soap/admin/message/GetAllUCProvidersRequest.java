// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.VoiceAdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Returns all installed UC providers and applicable UC service attributes for each
 * provider.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceAdminConstants.E_GET_ALL_UC_PROVIDERS_REQUEST)
public class GetAllUCProvidersRequest {
    public GetAllUCProvidersRequest() {
    }
}
