// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Set Password <br>
 *     <b>Access</b>: domain admin sufficient <br>
 *     note: this request is by default proxied to the account's home server
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_SET_PASSWORD_REQUEST)
@XmlType(propOrder = {})
public class SetPasswordRequest {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlAttribute(name = AdminConstants.E_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-description New PAssword
   */
  @XmlAttribute(name = AdminConstants.E_NEW_PASSWORD, required = true)
  private final String newPassword;

  @XmlElement(name = AccountConstants.E_DRYRUN, required = false)
  private boolean dryRun;

  /** no-argument constructor wanted by JAXB */
  public SetPasswordRequest() {
    this(null, null);
  }

  public SetPasswordRequest(String id, String newPassword) {
    this.id = id;
    this.newPassword = newPassword;
  }

  public SetPasswordRequest(String id, String newPassword, boolean dryRun) {
    this.id = id;
    this.newPassword = newPassword;
    setDryRun(dryRun);
  }

  public String getId() {
    return id;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public boolean isDryRun() {
    return dryRun;
  }

  public void setDryRun(boolean dryRun) {
    this.dryRun = dryRun;
  }
}
