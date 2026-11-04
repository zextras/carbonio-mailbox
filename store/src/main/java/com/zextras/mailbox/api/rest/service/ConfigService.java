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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigService {

  private final Supplier<Provisioning> provisioningSupplier;
  private final Supplier<AttributeManager> attributeManagerSupplier;
  private final Map<String, String> modifiableAttributes;

  public ConfigService(
      Supplier<Provisioning> provisioningSupplier,
      Supplier<AttributeManager> attributeManagerSupplier,
      Set<String> modifiableAttributes) {
    this.provisioningSupplier = provisioningSupplier;
    this.attributeManagerSupplier = attributeManagerSupplier;
    this.modifiableAttributes = modifiableAttributes.stream()
        .collect(Collectors.toMap(ConfigService::key, attribute -> attribute));
  }

  public Try<Void> modifyConfig(List<ConfigOperation> operations) {
    return Try.run(() -> {
      final Map<String, Object> modifications = toModifications(operations);
      if (modifications.isEmpty()) {
        return;
      }
      final Provisioning provisioning = provisioningSupplier.get();
      provisioning.modifyAttrs(provisioning.getConfig(), modifications, true);
    });
  }

  private Map<String, Object> toModifications(List<ConfigOperation> operations) {
    if (operations == null || operations.isEmpty()) {
      throw new InvalidConfigModificationException("Nothing to modify: provide at least one operation");
    }
    final AttributeManager attributeManager = attributeManagerSupplier.get();
    final Map<String, AttributeChange> changes = new LinkedHashMap<>();
    for (ConfigOperation operation : operations) {
      final String attribute = allowedAttribute(operation);
      apply(operation, attribute, attributeManager.isMultiValued(attribute),
          changes.computeIfAbsent(attribute, AttributeChange::new));
    }
    final Map<String, Object> modifications = new HashMap<>();
    changes.values().forEach(change -> change.putModifications(modifications));
    return modifications;
  }

  private String allowedAttribute(ConfigOperation operation) {
    if (operation == null || operation.attribute() == null || operation.attribute().isBlank()) {
      throw new InvalidConfigModificationException("Missing attribute in operation");
    }
    final String attribute = modifiableAttributes.get(key(operation.attribute()));
    if (attribute == null) {
      throw new InvalidConfigModificationException("Attribute cannot be modified: " + operation.attribute());
    }
    return attribute;
  }

  private static void apply(ConfigOperation operation, String attribute, boolean multiValued, AttributeChange change) {
    switch (operation.op() == null ? "" : operation.op()) {
      case "set" -> {
        validateValues(attribute, operation.values());
        if (!multiValued && operation.values().size() > 1) {
          throw new InvalidConfigModificationException(
              "Attribute is single-valued, set accepts one value: " + attribute);
        }
        change.set(operation.values());
      }
      case "unset" -> {
        if (operation.values() != null) {
          throw new InvalidConfigModificationException("unset takes no values: " + attribute);
        }
        change.unset();
      }
      case "add" -> {
        validateValueChange(attribute, operation.values(), multiValued);
        change.add(operation.values());
      }
      case "remove" -> {
        validateValueChange(attribute, operation.values(), multiValued);
        change.remove(operation.values());
      }
      default -> throw new InvalidConfigModificationException(
          "Unknown operation '" + operation.op() + "', expected set, unset, add or remove");
    }
  }

  private static void validateValueChange(String attribute, List<String> values, boolean multiValued) {
    if (!multiValued) {
      throw new InvalidConfigModificationException("Attribute is single-valued, use set or unset: " + attribute);
    }
    validateValues(attribute, values);
  }

  private static void validateValues(String attribute, List<String> values) {
    if (values == null || values.isEmpty() || values.stream().anyMatch(value -> value == null || value.isBlank())) {
      throw new InvalidConfigModificationException("Missing or empty value for attribute: " + attribute);
    }
  }

  private static String key(String attribute) {
    return attribute.toLowerCase(Locale.ROOT);
  }
}
