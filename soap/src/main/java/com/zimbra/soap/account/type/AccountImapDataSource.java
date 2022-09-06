// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.zimbra.soap.type.DataSource;
import com.zimbra.soap.type.ImapDataSource;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {})
public class AccountImapDataSource extends AccountDataSource implements ImapDataSource {

  public AccountImapDataSource() {}

  public AccountImapDataSource(DataSource data) {
    super(data);
  }
}
