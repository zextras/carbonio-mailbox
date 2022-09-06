// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.XMbxSearchConstants;
import com.zimbra.soap.admin.type.AdminKeyValuePairs;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

// Note: ZimbraXMbxSearch/docs/soap.txt documented a non-existent <searchtask> sub-element.
//       This is not used - the attributes are direct children of <CreateXMbxSearchRequest>
/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Creates a search task
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = XMbxSearchConstants.E_CREATE_XMBX_SEARCH_REQUEST)
public class CreateXMbxSearchRequest extends AdminKeyValuePairs {

  public CreateXMbxSearchRequest() {}
}
