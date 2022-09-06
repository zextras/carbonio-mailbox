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
 * @zm-api-command-description Get the intersection of installed skins on the server and the list
 *     specified in the <b>zimbraAvailableSkin</b> on an account (or its CoS). If none is set in
 *     <b>zimbraAvailableSkin</b>, get the entire list of installed skins. The installed skin list
 *     is obtained by a directory scan of the designated location of skins on a server.
 */
@XmlRootElement(name = AccountConstants.E_GET_AVAILABLE_SKINS_REQUEST)
public class GetAvailableSkinsRequest {}
