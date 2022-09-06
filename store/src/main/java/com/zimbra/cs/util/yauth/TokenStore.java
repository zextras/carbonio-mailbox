// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.yauth;

import java.io.IOException;

public abstract class TokenStore {
  public String newToken(String appId, String user, String pass)
      throws AuthenticationException, IOException {
    removeToken(appId, user);
    String token = RawAuth.getToken(appId, user, pass);
    putToken(appId, user, token);
    return token;
  }

  public boolean hasToken(String appId, String user) {
    return getToken(appId, user) != null;
  }

  protected abstract void putToken(String appId, String user, String token);

  public abstract String getToken(String appId, String user);

  public abstract void removeToken(String appId, String user);

  public abstract int size();
}
