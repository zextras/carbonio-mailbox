// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

public abstract class FetchResponseHandler implements ResponseHandler {
  private boolean dispose;

  public FetchResponseHandler(boolean dispose) {
    this.dispose = dispose;
  }

  public FetchResponseHandler() {
    this(true);
  }

  public void handleResponse(ImapResponse res) throws Exception {
    if (res.getCCode() == CAtom.FETCH) {
      MessageData md = (MessageData) res.getData();
      try {
        handleFetchResponse(md);
      } finally {
        if (dispose) md.dispose();
      }
    }
  }

  public abstract void handleFetchResponse(MessageData md) throws Exception;
}
