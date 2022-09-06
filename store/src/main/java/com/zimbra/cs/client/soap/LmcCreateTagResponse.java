// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcCreateTagResponse extends LmcSoapResponse {

  private LmcTag mTag;

  public LmcTag getTag() {
    return mTag;
  }

  public void setTag(LmcTag t) {
    mTag = t;
  }
}
