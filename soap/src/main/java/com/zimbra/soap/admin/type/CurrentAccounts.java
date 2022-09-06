// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.BackupConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class CurrentAccounts {

  // soapbackup.txt implies there is a "more" attribute also but code doesn't add that.
  /**
   * @zm-api-field-tag current-acct-total
   * @zm-api-field-description Total number of accounts currently being backed up
   */
  @XmlAttribute(name = BackupConstants.A_TOTAL_COUNT /* total */, required = false)
  private Integer totalCount;

  /**
   * @zm-api-field-description Information on accounts
   */
  @XmlElement(name = BackupConstants.E_ACCOUNT /* account */, required = false)
  private List<Name> accounts = Lists.newArrayList();

  public CurrentAccounts() {}

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public void setAccounts(Iterable<Name> accounts) {
    this.accounts.clear();
    if (accounts != null) {
      Iterables.addAll(this.accounts, accounts);
    }
  }

  public void addAccount(Name account) {
    this.accounts.add(account);
  }

  public Integer getTotalCount() {
    return totalCount;
  }

  public List<Name> getAccounts() {
    return Collections.unmodifiableList(accounts);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("totalCount", totalCount).add("accounts", accounts);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
