/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ConfigChanges(
    Map<String, List<String>> set,
    List<String> unset,
    Map<String, List<String>> add,
    Map<String, List<String>> remove) {

  public ConfigChanges {
    set = set == null ? Map.of() : set;
    unset = unset == null ? List.of() : unset;
    add = add == null ? Map.of() : add;
    remove = remove == null ? Map.of() : remove;
  }

  public boolean isEmpty() {
    return set.isEmpty() && unset.isEmpty() && add.isEmpty() && remove.isEmpty();
  }

  public Set<String> attributes() {
    final Set<String> attributes = new HashSet<>(set.keySet());
    attributes.addAll(unset);
    attributes.addAll(add.keySet());
    attributes.addAll(remove.keySet());
    return attributes;
  }

  public boolean isReplaced(String attribute) {
    return set.containsKey(attribute) || unset.contains(attribute);
  }

  public boolean isChangedByValue(String attribute) {
    return add.containsKey(attribute) || remove.containsKey(attribute);
  }
}
