// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client;

public class LmcBrowseData {

  private String mFlags;
  private String mData;

  public void setFlags(String f) {
    mFlags = f;
  }

  public void setData(String d) {
    mData = d;
  }

  public String getFlags() {
    return mFlags;
  }

  public String getData() {
    return mData;
  }
}
