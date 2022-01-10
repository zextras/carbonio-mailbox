// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.VoiceConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Unified Communications information
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_GET_UC_INFO_REQUEST)
public class GetUCInfoRequest {

    public GetUCInfoRequest() {
    }

}
