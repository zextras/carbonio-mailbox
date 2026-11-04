/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

sealed interface AttributeChange {

  record Replace(Set<String> values) implements AttributeChange {

  }

  record Delta(Set<String> added, Set<String> removed) implements AttributeChange {

  }

  static AttributeChange of(ConfigOperation operation) {
    return switch (operation) {
      case ConfigOperation.Set set -> new Replace(ordered(set.values()));
      case ConfigOperation.Unset ignored -> new Replace(Set.of());
      case ConfigOperation.Add add -> new Delta(ordered(add.values()), Set.of());
      case ConfigOperation.Remove remove -> new Delta(Set.of(), ordered(remove.values()));
    };
  }

  default AttributeChange then(AttributeChange next) {
    return switch (next) {
      case Replace replace -> replace;
      case Delta delta -> switch (this) {
        case Replace replace -> new Replace(union(minus(replace.values(), delta.removed()), delta.added()));
        case Delta previous -> new Delta(
            union(minus(previous.added(), delta.removed()), delta.added()),
            union(minus(previous.removed(), delta.added()), delta.removed()));
      };
    };
  }

  default Map<String, Object> modifications(String attribute) {
    return switch (this) {
      case Replace replace -> Map.of(attribute, replace.values().isEmpty() ? "" : toArray(replace.values()));
      case Delta delta -> Stream.of(Map.entry("+" + attribute, delta.added()), Map.entry("-" + attribute, delta.removed()))
          .filter(entry -> !entry.getValue().isEmpty())
          .collect(Collectors.toMap(Map.Entry::getKey, entry -> toArray(entry.getValue())));
    };
  }

  private static Object toArray(Set<String> values) {
    return values.toArray(String[]::new);
  }

  private static Set<String> ordered(Collection<String> values) {
    return Collections.unmodifiableSet(new LinkedHashSet<>(values));
  }

  private static Set<String> minus(Set<String> values, Set<String> excluded) {
    return ordered(values.stream().filter(value -> !excluded.contains(value)).toList());
  }

  private static Set<String> union(Set<String> first, Set<String> second) {
    return ordered(Stream.concat(first.stream(), second.stream()).toList());
  }
}
