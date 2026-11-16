// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zimbra.common.util.ExceptionToString;
import org.json.JSONException;


public interface ToZJSONObject {

  ObjectNode toObjectNode();

  static String toString(ToZJSONObject zjo) {
    try {
      return zjo.toObjectNode().toString();
    } catch (JSONException e) {
      return ExceptionToString.ToString(e);
    }
  }
}
