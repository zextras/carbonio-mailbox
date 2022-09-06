// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Returns all account loggers that have been created on the given
 *     server since the last server start.
 */
@XmlRootElement(name = AdminConstants.E_GET_ALL_ACCOUNT_LOGGERS_REQUEST)
public class GetAllAccountLoggersRequest {

  public GetAllAccountLoggersRequest() {}
}
