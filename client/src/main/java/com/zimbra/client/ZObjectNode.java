// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;

public final class ZObjectNode {

  private ZObjectNode() {}

  public static void putMap(ObjectNode node, String key, Map<String, String> attrs) {
    ObjectNode obj = node.putObject(key);
    if (attrs != null) {
      for (Map.Entry<String, String> entry : attrs.entrySet()) {
        if (entry.getValue() != null) {
          obj.put(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  public static void put(ObjectNode node, String key, List<? extends ToZJSONObject> items) {
    ArrayNode array = node.putArray(key);
    if (items != null) {
      for (ToZJSONObject item : items) {
        array.add(item.toObjectNode());
      }
    }
  }

  public static void putList(ObjectNode node, String key, List<String> items) {
    ArrayNode array = node.putArray(key);
    if (items != null) {
      for (String item : items) {
        array.add(item);
      }
    }
  }
}
