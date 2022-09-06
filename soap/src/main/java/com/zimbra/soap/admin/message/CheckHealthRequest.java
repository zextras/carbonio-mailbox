// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required false - leave the auth decision entirely up to Admin auth
 * @zm-api-command-admin-auth-required maybe - No auth required if client is localhost. Otherwise,
 *     admin auth is required.
 * @zm-api-command-description Check Health
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHECK_HEALTH_REQUEST)
@XmlType(propOrder = {})
public class CheckHealthRequest {
  public CheckHealthRequest() {}
}
