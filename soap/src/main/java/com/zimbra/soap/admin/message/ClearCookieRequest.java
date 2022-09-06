// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CookieSpec;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required false
 * @zm-api-command-admin-auth-required false - always allow clearing
 * @zm-api-command-description Clear cookie
 */
@XmlRootElement(name = AdminConstants.E_CLEAR_COOKIE_REQUEST)
public class ClearCookieRequest {

  /**
   * @zm-api-field-description Specifies cookies to clean
   */
  @XmlElement(name = AdminConstants.E_COOKIE)
  private List<CookieSpec> cookies = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ClearCookieRequest() {}

  public ClearCookieRequest(List<CookieSpec> cookies) {
    this.cookies = cookies;
  }

  public void addCookie(CookieSpec cookie) {
    cookies.add(cookie);
  }
}
