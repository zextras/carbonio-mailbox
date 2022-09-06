// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.ArchiveConstants;
import com.zimbra.soap.admin.type.ArchiveSpec;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Enable Archive
 *     <ul>
 *       <li>Archive account is created by default based on name templates. You can suppress this by
 *           sending create=0. This is useful if you are going to use a third party system to do the
 *           archiving and ZCS is just a mail forker.
 *       <li>- Recommended that password not be specified so only admins can login.
 *       <li>A newly created archive account is always defaulted with the following attributes. You
 *           can override these attributes (or set additional ones) by specifying <b>&lt;a></b>
 *           elements in <b>&lt;archive></b>.
 *           <pre>
 *          amavisBypassSpamChecks: TRUE
 *          amavisBypassVirusChecks: TRUE
 *          zimbraHideInGal: TRUE
 *          zimbraIsSystemResource: TRUE
 *          zimbraMailQuota: 0
 *      </pre>
 *       <li><b>&lt;password></b>, <b>&lt;cos></b> and <b>&lt;a></b> (attr) elements matter only if
 *           create=1.
 *     </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = ArchiveConstants.E_ENABLE_ARCHIVE_REQUEST)
public class EnableArchiveRequest {

  /**
   * @zm-api-field-description Account
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT /* account */, required = true)
  private final AccountSelector account;

  /**
   * @zm-api-field-description Archive details
   */
  @XmlElement(name = ArchiveConstants.E_ARCHIVE /* archive */, required = false)
  private ArchiveSpec archive;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private EnableArchiveRequest() {
    this((AccountSelector) null);
  }

  public EnableArchiveRequest(AccountSelector account) {
    this.account = account;
  }

  public void setArchive(ArchiveSpec archive) {
    this.archive = archive;
  }

  public AccountSelector getAccount() {
    return account;
  }

  public ArchiveSpec getArchive() {
    return archive;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("account", account).add("archive", archive);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
