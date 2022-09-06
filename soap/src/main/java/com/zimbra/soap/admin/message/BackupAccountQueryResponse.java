// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.BackupAccountQueryInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = BackupConstants.E_BACKUP_ACCOUNT_QUERY_RESPONSE)
@XmlType(propOrder = {})
public class BackupAccountQueryResponse {

  /**
   * @zm-api-field-description Account backup details
   */
  @XmlElement(name = BackupConstants.E_ACCOUNT /* account */, required = false)
  private List<BackupAccountQueryInfo> accounts = Lists.newArrayList();

  public BackupAccountQueryResponse() {}

  public void setAccounts(Iterable<BackupAccountQueryInfo> accounts) {
    this.accounts.clear();
    if (accounts != null) {
      Iterables.addAll(this.accounts, accounts);
    }
  }

  public void addAccount(BackupAccountQueryInfo account) {
    this.accounts.add(account);
  }

  public List<BackupAccountQueryInfo> getAccounts() {
    return Collections.unmodifiableList(accounts);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("accounts", accounts);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
