// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;

/**
 * Jackson counterpart of {@link ZJSONObject}: same overloads and same null handling, so that
 * {@link ToZJSONObject#toObjectNode()} produces the same document as {@link
 * ToZJSONObject#toZJSONObject()}. As with org.json, putting a null String removes the key.
 */
public class ZObjectNode {

  private final ObjectNode node;

  public ZObjectNode() {
    this(JsonNodeFactory.instance.objectNode());
  }

  public ZObjectNode(ObjectNode node) {
    this.node = node;
  }

  public ObjectNode getObjectNode() {
    return node;
  }

  public ZObjectNode put(String key, ToZJSONObject value) {
    if (value != null) {
      node.set(key, value.toObjectNode());
    }
    return this;
  }

  public ZObjectNode put(String key, ObjectNode value) {
    if (value != null) {
      node.set(key, value);
    }
    return this;
  }

  public ZObjectNode put(String key, String value) {
    putOrRemove(node, key, value);
    return this;
  }

  public ZObjectNode put(String key, long value) {
    node.put(key, value);
    return this;
  }

  public ZObjectNode put(String key, boolean value) {
    node.put(key, value);
    return this;
  }

  public ZObjectNode putMap(String key, Map<String, String> attrs) {
    ObjectNode obj = node.putObject(key);
    if (attrs != null) {
      for (Map.Entry<String, String> entry : attrs.entrySet()) {
        putOrRemove(obj, entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  public ZObjectNode put(String key, List<? extends ToZJSONObject> items) {
    ArrayNode array = node.putArray(key);
    if (items != null) {
      for (ToZJSONObject item : items) {
        array.add(item.toObjectNode());
      }
    }
    return this;
  }

  public ZObjectNode putList(String key, List<String> items) {
    ArrayNode array = node.putArray(key);
    if (items != null) {
      for (String item : items) {
        array.add(item);
      }
    }
    return this;
  }

  /** Mirrors org.json {@code JSONObject.put(key, null)}, which removes the key. */
  static void putOrRemove(ObjectNode target, String key, String value) {
    if (value == null) {
      target.remove(key);
    } else {
      target.put(key, value);
    }
  }
}
