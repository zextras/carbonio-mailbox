// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required false
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Version information
 */
@XmlRootElement(name = AdminConstants.E_GET_VERSION_INFO_REQUEST)
public class GetVersionInfoRequest {
  public GetVersionInfoRequest() {}
}
