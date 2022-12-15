// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get the intersection of all translated locales installed on the server and the list
 * specified in zimbraAvailableLocale.  The locale list in the response is sorted by display name (name attribute).
 */
@XmlRootElement(name=AccountConstants.E_GET_AVAILABLE_LOCALES_REQUEST)
public class GetAvailableLocalesRequest {
}
