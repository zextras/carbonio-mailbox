// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.files.client;

import java.util.Map;

public class GraphQlResponse<T> {

  public GraphQlResponse() {
  }

  public Map<String, T> getData() {
    return data;
  }

  public T getBody() {
    return data.get("data");
  }

  private Map<String, T> data;

}
