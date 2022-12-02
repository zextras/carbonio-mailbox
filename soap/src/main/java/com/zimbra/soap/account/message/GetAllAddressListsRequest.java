// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-description Returns all address lists which are there in the user's domain
 */
@XmlRootElement(name=AccountConstants.E_GET_ALL_ADDRESS_LISTS_REQUEST)
public class GetAllAddressListsRequest {
}
