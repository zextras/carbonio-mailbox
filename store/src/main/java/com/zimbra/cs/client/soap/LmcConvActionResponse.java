// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

public class LmcConvActionResponse extends LmcSoapResponse {

  private String mIDList;
  private String mOp;

  public String getConvList() {
    return mIDList;
  }

  public String getOp() {
    return mOp;
  }

  public void setConvList(String idList) {
    mIDList = idList;
  }

  public void setOp(String op) {
    mOp = op;
  }
}
