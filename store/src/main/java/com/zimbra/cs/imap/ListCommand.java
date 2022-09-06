// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.Set;

public class ListCommand extends AbstractListCommand {

  private byte selectOptions;
  private byte returnOptions;
  private byte status;

  public ListCommand(
      String referenceName,
      Set<String> mailboxNames,
      byte selectOptions,
      byte returnOptions,
      byte status) {
    super(referenceName, mailboxNames);
    this.selectOptions = selectOptions;
    this.returnOptions = returnOptions;
    this.status = status;
  }

  public byte getSelectOptions() {
    return selectOptions;
  }

  public byte getReturnOptions() {
    return returnOptions;
  }

  public byte getStatus() {
    return status;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + returnOptions;
    result = prime * result + selectOptions;
    result = prime * result + status;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ListCommand other = (ListCommand) obj;
    if (returnOptions != other.returnOptions) {
      return false;
    }
    if (selectOptions != other.selectOptions) {
      return false;
    }
    if (status != other.status) {
      return false;
    }
    return true;
  }
}
