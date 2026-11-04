/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Provisioning;
import io.vavr.control.Try;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ConfigService {

  private final Supplier<Provisioning> provisioningSupplier;
  private final Supplier<AttributeManager> attributeManagerSupplier;
  private final Set<String> modifiableAttributes;

  public ConfigService(
      Supplier<Provisioning> provisioningSupplier,
      Supplier<AttributeManager> attributeManagerSupplier,
      Set<String> modifiableAttributes) {
    this.provisioningSupplier = provisioningSupplier;
    this.attributeManagerSupplier = attributeManagerSupplier;
    this.modifiableAttributes = modifiableAttributes;
  }

  public Try<Void> modifyConfig(ConfigChanges changes) {
    return Try.run(() -> {
      validate(changes);
      final Provisioning provisioning = provisioningSupplier.get();
      provisioning.modifyAttrs(provisioning.getConfig(), toModifications(changes), true);
    });
  }

  private void validate(ConfigChanges changes) {
    if (changes.isEmpty()) {
      throw new InvalidConfigModificationException(
          "Nothing to modify: provide 'set', 'unset', 'add' or 'remove'");
    }
    final AttributeManager attributeManager = attributeManagerSupplier.get();
    for (String attribute : changes.attributes()) {
      validateAttribute(attribute, changes, attributeManager.isMultiValued(attribute));
    }
  }

  private void validateAttribute(String attribute, ConfigChanges changes, boolean multiValued) {
    if (!modifiableAttributes.contains(attribute)) {
      throw new InvalidConfigModificationException("Attribute cannot be modified: " + attribute);
    }
    if (changes.isReplaced(attribute) && countOperations(attribute, changes) > 1) {
      throw new InvalidConfigModificationException("Attribute in more than one operation: " + attribute);
    }
    if (changes.set().containsKey(attribute)) {
      validateSet(attribute, changes.set().get(attribute), multiValued);
    }
    if (changes.isChangedByValue(attribute)) {
      validateValueChange(attribute, changes.add().get(attribute), changes.remove().get(attribute), multiValued);
    }
  }

  private static long countOperations(String attribute, ConfigChanges changes) {
    return List.of(
            changes.set().containsKey(attribute),
            changes.unset().contains(attribute),
            changes.add().containsKey(attribute),
            changes.remove().containsKey(attribute))
        .stream()
        .filter(Boolean::booleanValue)
        .count();
  }

  private static void validateSet(String attribute, List<String> values, boolean multiValued) {
    validateValues(attribute, values);
    if (!multiValued && values.size() > 1) {
      throw new InvalidConfigModificationException(
          "Attribute is single-valued, set accepts one value: " + attribute);
    }
  }

  private static void validateValueChange(
      String attribute, List<String> added, List<String> removed, boolean multiValued) {
    if (!multiValued) {
      throw new InvalidConfigModificationException(
          "Attribute is single-valued, use set or unset: " + attribute);
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

  private static Map<String, Object> toModifications(ConfigChanges changes) {
    final Map<String, Object> modifications = new HashMap<>();
    changes.set().forEach((attribute, values) -> modifications.put(attribute, values.toArray(String[]::new)));
    changes.unset().forEach(attribute -> modifications.put(attribute, ""));
    changes.add().forEach((attribute, values) -> modifications.put("+" + attribute, values.toArray(String[]::new)));
    changes.remove().forEach((attribute, values) -> modifications.put("-" + attribute, values.toArray(String[]::new)));
    return modifications;
  }
}
