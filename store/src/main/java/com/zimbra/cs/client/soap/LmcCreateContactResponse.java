// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcCreateContactResponse extends LmcSoapResponse {

  private LmcContact mContact;

  public void setContact(LmcContact c) {
    mContact = c;
  }

  public LmcContact getContact() {
    return mContact;
  }
}
