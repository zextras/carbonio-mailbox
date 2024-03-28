// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.files.client;

import com.zimbra.common.util.ZimbraCookie;

public class Token {

  private final String value;

  public Token(String value) {
    this.value = value;
  }

  public String getCookie() {
    return ZimbraCookie.COOKIE_ZM_AUTH_TOKEN + "=" + value;
  }
}
