// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;

/**
 * @zm-api-command-auth-required false - if version information shouldn't be exposed a fault will be thrown
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Version information
 * <br>
 * Note: This request will return a SOAP fault if the <b>zimbraSoapExposeVersion</b> server/globalconfig attribute is
 * set to FALSE.
 */
@XmlRootElement(name=AccountConstants.E_GET_VERSION_INFO_REQUEST)
public class GetVersionInfoRequest {
}
