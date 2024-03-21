// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.files.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class GraphQlResponse {

  public GraphQlResponse() {
    // empty constructor needed by Jackson
  }

  public JsonNode getData() {
    return data;
  }

  @JsonProperty("data")
  private JsonNode data;

}
