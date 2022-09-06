// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Check whether a hostname can be resolved
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHECK_HOSTNAME_RESOLVE_REQUEST)
public class CheckHostnameResolveRequest {

  /**
   * @zm-api-field-description Hostname
   */
  @XmlAttribute(name = AdminConstants.E_HOSTNAME, required = false)
  private final String hostname;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CheckHostnameResolveRequest() {
    this((String) null);
  }

  public CheckHostnameResolveRequest(String hostname) {
    this.hostname = hostname;
  }

  public String getHostname() {
    return hostname;
  }
}
