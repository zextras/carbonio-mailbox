// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

public class ActionRedirect extends org.apache.jsieve.mail.ActionRedirect {

  private boolean copy;

  public ActionRedirect(String address) {
    this(address, false);
  }

  public ActionRedirect(String address, boolean copy) {
    super(address);
    this.setCopy(copy);
  }

  public boolean isCopy() {
    return copy;
  }

  public void setCopy(boolean copy) {
    this.copy = copy;
  }
}
