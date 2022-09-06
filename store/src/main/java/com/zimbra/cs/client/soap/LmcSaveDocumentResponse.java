// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcSaveDocumentResponse extends LmcSoapResponse {

  private LmcDocument mDoc;

  public LmcDocument getDocument() {
    return mDoc;
  }

  public void setDocument(LmcDocument doc) {
    mDoc = doc;
  }
}
