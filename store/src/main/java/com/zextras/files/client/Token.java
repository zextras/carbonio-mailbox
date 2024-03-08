// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.files.client;

public class Token {

  public String getValue() {
    return value;
  }

  private final String value;

  public Token(String value) {
    this.value = value;
  }
}
