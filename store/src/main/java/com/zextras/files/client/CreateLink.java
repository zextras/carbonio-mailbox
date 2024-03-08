// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.files.client;

public class CreateLink {

  private final String url;

  public CreateLink(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
