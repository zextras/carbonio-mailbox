// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Returns all locales defined in the system. This is the same list
 *     returned by java.util.Locale.getAvailableLocales(), sorted by display name (name attribute).
 */
@XmlRootElement(name = AccountConstants.E_GET_ALL_LOCALES_REQUEST)
public class GetAllLocalesRequest {}
