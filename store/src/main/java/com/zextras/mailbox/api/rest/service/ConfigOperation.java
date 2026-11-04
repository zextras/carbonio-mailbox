/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import java.util.List;

public record ConfigOperation(String attribute, String op, List<String> values) {

  public static ConfigOperation set(String attribute, String... values) {
    return new ConfigOperation(attribute, "set", List.of(values));
  }

  public static ConfigOperation unset(String attribute) {
    return new ConfigOperation(attribute, "unset", null);
  }

  public static ConfigOperation add(String attribute, String... values) {
    return new ConfigOperation(attribute, "add", List.of(values));
  }

  public static ConfigOperation remove(String attribute, String... values) {
    return new ConfigOperation(attribute, "remove", List.of(values));
  }
}
