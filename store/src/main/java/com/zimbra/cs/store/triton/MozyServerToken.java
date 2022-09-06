// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.triton;

import org.apache.http.HttpResponse;

/** String wrapper so Mozy token can be passed around and updated on each request if necessary */
public class MozyServerToken {
  private String token;

  public MozyServerToken() {
    super();
  }

  public String getToken() {
    return token;
  }

  /** Set token value based on TDS response header contained in HttpMethod */
  public void setToken(HttpResponse resp) {
    this.token = resp.getFirstHeader(TritonHeaders.SERVER_TOKEN).getValue();
  }
}
