/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.cs.account.Provisioning;
import io.vavr.control.Try;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ConfigService {

  private final Supplier<Provisioning> provisioningSupplier;
  private final Set<String> modifiableAttributes;

  public ConfigService(Supplier<Provisioning> provisioningSupplier, Set<String> modifiableAttributes) {
    this.provisioningSupplier = provisioningSupplier;
    this.modifiableAttributes = modifiableAttributes;
  }

  public Try<Void> modifyConfig(Map<String, List<String>> valuesToAdd, Map<String, List<String>> valuesToRemove) {
    return Try.run(() -> {
      final Map<String, List<String>> add = valuesToAdd == null ? Map.of() : valuesToAdd;
      final Map<String, List<String>> remove = valuesToRemove == null ? Map.of() : valuesToRemove;
      validate(add, remove);
      final Provisioning provisioning = provisioningSupplier.get();
      provisioning.modifyAttrs(provisioning.getConfig(), toModifications(add, remove), true);
    });
  }

  private void validate(Map<String, List<String>> add, Map<String, List<String>> remove) {
    if (add.isEmpty() && remove.isEmpty()) {
      throw new InvalidConfigModificationException("Nothing to modify: provide 'add' or 'remove'");
    }
    final Set<String> attributes = new HashSet<>(add.keySet());
    attributes.addAll(remove.keySet());
    for (String attribute : attributes) {
      validateAttribute(attribute, add.get(attribute), remove.get(attribute));
    }
  }

  private void validateAttribute(String attribute, List<String> added, List<String> removed) {
    if (!modifiableAttributes.contains(attribute)) {
      throw new InvalidConfigModificationException("Attribute cannot be modified: " + attribute);
    }
    validateValues(attribute, added);
    validateValues(attribute, removed);
    if (added != null && removed != null && added.stream().anyMatch(removed::contains)) {
      throw new InvalidConfigModificationException("Same value added and removed for attribute: " + attribute);
    }
  }

  private static void validateValues(String attribute, List<String> values) {
    if (values == null) {
      return;
    }
    if (values.isEmpty() || values.stream().anyMatch(value -> value == null || value.isBlank())) {
      throw new InvalidConfigModificationException("Empty value for attribute: " + attribute);
    }
  }

  private static Map<String, Object> toModifications(Map<String, List<String>> add, Map<String, List<String>> remove) {
    final Map<String, Object> modifications = new HashMap<>();
    add.forEach((attribute, values) -> modifications.put("+" + attribute, values.toArray(String[]::new)));
    remove.forEach((attribute, values) -> modifications.put("-" + attribute, values.toArray(String[]::new)));
    return modifications;
  }
}
