// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.util.MapUtil;
import com.zimbra.soap.account.message.AuthResponse;
import com.zimbra.soap.account.type.Session;
import java.util.List;
import java.util.Map;

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

  public String getSessionId() {
    Session session = data.getSession();
    if (session == null) {
      return null;
    }
    return session.getId();
  }

  void setSessionId(String id) {
    Session session = data.getSession();
    if (session == null) {
      session = new Session();
      data.setSession(session);
    }
    session.setId(id);
  }

  public long getExpires() {
    return expires;
  }

  public long getLifetime() {
    return data.getLifetime();
  }

  public String getRefer() {
    return data.getRefer();
  }

  public Map<String, List<String>> getAttrs() {
    return MapUtil.multimapToMapOfLists(data.getAttrsMultimap());
  }

  public Map<String, List<String>> getPrefs() {
    return MapUtil.multimapToMapOfLists(data.getPrefsMultimap());
  }
}
