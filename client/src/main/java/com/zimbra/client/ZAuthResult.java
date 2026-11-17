// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.soap.account.message.AuthResponse;
import com.zimbra.soap.account.type.Session;

public class ZAuthResult {

  private long expires;
  private AuthResponse data;

  public ZAuthResult(AuthResponse res) {
    data = res;
    expires = data.getLifetime() + System.currentTimeMillis();
  }

  public ZAuthToken getAuthToken() {
    return new ZAuthToken(data.getAuthToken());
  }

	void setSessionId(String id) {
    Session session = data.getSession();
    if (session == null) {
      session = new Session();
      data.setSession(session);
    }
    session.setId(id);
  }

	public String getCsrfToken() {
    return data.getCsrfToken();
  }
}
