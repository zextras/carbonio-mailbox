/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import java.util.List;

public sealed interface ConfigOperation {

  String attribute();

  record Set(String attribute, List<String> values) implements ConfigOperation {

    public Set {
      values = List.copyOf(values);
    }
  }

  record Unset(String attribute) implements ConfigOperation {

  }

  record Add(String attribute, List<String> values) implements ConfigOperation {

    public Add {
      values = List.copyOf(values);
    }
  }

  record Remove(String attribute, List<String> values) implements ConfigOperation {

    public Remove {
      values = List.copyOf(values);
    }
  }

  static ConfigOperation set(String attribute, String... values) {
    return new Set(attribute, List.of(values));
  }

  static ConfigOperation unset(String attribute) {
    return new Unset(attribute);
  }

  static ConfigOperation add(String attribute, String... values) {
    return new Add(attribute, List.of(values));
  }

  static ConfigOperation remove(String attribute, String... values) {
    return new Remove(attribute, List.of(values));
  }
}
