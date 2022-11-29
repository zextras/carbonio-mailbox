// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/**
 *
 */
package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

/**
 * @author gsolovyev
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_REFRESH_REGISTERED_AUTHTOKENS_RESPONSE)
public class RefreshRegisteredAuthTokensResponse {

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    public RefreshRegisteredAuthTokensResponse() {
    }

}
