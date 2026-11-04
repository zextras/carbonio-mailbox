/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AttributeChange {

  private final String attribute;
  private boolean replacing;
  private final Set<String> replacement = new LinkedHashSet<>();
  private final Set<String> added = new LinkedHashSet<>();
  private final Set<String> removed = new LinkedHashSet<>();

  AttributeChange(String attribute) {
    this.attribute = attribute;
  }

  void set(List<String> values) {
    replacing = true;
    replacement.clear();
    replacement.addAll(values);
    added.clear();
    removed.clear();
  }

  void unset() {
    set(List.of());
  }

  void add(List<String> values) {
    if (replacing) {
      replacement.addAll(values);
      return;
    }
    added.addAll(values);
    values.forEach(removed::remove);
  }

  void remove(List<String> values) {
    if (replacing) {
      values.forEach(replacement::remove);
      return;
    }
    removed.addAll(values);
    values.forEach(added::remove);
  }

  void putModifications(Map<String, Object> modifications) {
    if (replacing) {
      modifications.put(attribute, replacement.isEmpty() ? "" : replacement.toArray(String[]::new));
      return;
    }
    if (!added.isEmpty()) {
      modifications.put("+" + attribute, added.toArray(String[]::new));
    }
    if (!removed.isEmpty()) {
      modifications.put("-" + attribute, removed.toArray(String[]::new));
    }
  }
}
