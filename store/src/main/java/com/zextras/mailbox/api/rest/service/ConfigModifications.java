/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ConfigModifications {

  private final Map<String, Set<String>> replace = new LinkedHashMap<>();
  private final Map<String, Set<String>> add = new LinkedHashMap<>();
  private final Map<String, Set<String>> remove = new LinkedHashMap<>();

  private ConfigModifications() {
  }

  static Map<String, Object> of(List<ConfigOperation> operations) {
    final ConfigModifications modifications = new ConfigModifications();
    operations.forEach(modifications::apply);
    return modifications.toMap();
  }

  private void apply(ConfigOperation operation) {
    final String attribute = operation.attribute();
    switch (operation) {
      case ConfigOperation.Set set -> replaceWith(attribute, set.values());
      case ConfigOperation.Unset ignored -> replaceWith(attribute, List.of());
      case ConfigOperation.Add added -> {
        if (replace.containsKey(attribute)) {
          replace.get(attribute).addAll(added.values());
        } else {
          valuesOf(add, attribute).addAll(added.values());
          valuesOf(remove, attribute).removeAll(added.values());
        }
      }
      case ConfigOperation.Remove removed -> {
        if (replace.containsKey(attribute)) {
          replace.get(attribute).removeAll(removed.values());
        } else {
          valuesOf(remove, attribute).addAll(removed.values());
          valuesOf(add, attribute).removeAll(removed.values());
        }
      }
    }
  }

  private void replaceWith(String attribute, List<String> values) {
    add.remove(attribute);
    remove.remove(attribute);
    replace.put(attribute, new LinkedHashSet<>(values));
  }

  private static Set<String> valuesOf(Map<String, Set<String>> values, String attribute) {
    return values.computeIfAbsent(attribute, ignored -> new LinkedHashSet<>());
  }

  private Map<String, Object> toMap() {
    final Map<String, Object> modifications = new HashMap<>();
    replace.forEach((attribute, values) -> modifications.put(attribute, values.isEmpty() ? "" : toArray(values)));
    add.forEach((attribute, values) -> putIfNotEmpty(modifications, "+" + attribute, values));
    remove.forEach((attribute, values) -> putIfNotEmpty(modifications, "-" + attribute, values));
    return modifications;
  }

  private static void putIfNotEmpty(Map<String, Object> modifications, String key, Set<String> values) {
    if (!values.isEmpty()) {
      modifications.put(key, toArray(values));
    }
  }

  private static String[] toArray(Set<String> values) {
    return values.toArray(String[]::new);
  }
}
