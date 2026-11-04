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
import java.util.Optional;
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
    if (operations.isEmpty()) {
      throw new InvalidConfigModificationException("Nothing to modify: provide at least one operation");
    }
    final AttributeManager attributeManager = attributeManagerSupplier.get();
    final Map<String, AttributeChange> changes = new LinkedHashMap<>();
    for (ConfigOperation operation : operations) {
      final String attribute = allowedAttribute(operation.attribute());
      apply(operation, attribute, attributeManager.isMultiValued(attribute),
          changes.computeIfAbsent(attribute, AttributeChange::new));
    }
    final Map<String, Object> modifications = new HashMap<>();
    changes.values().forEach(change -> change.putModifications(modifications));
    return modifications;
  }

  private String allowedAttribute(String requested) {
    return Optional.ofNullable(modifiableAttributes.get(key(requested)))
        .orElseThrow(() -> new InvalidConfigModificationException("Attribute cannot be modified: " + requested));
  }

  private static void apply(ConfigOperation operation, String attribute, boolean multiValued, AttributeChange change) {
    switch (operation) {
      case ConfigOperation.Set set -> {
        if (!multiValued && set.values().size() > 1) {
          throw new InvalidConfigModificationException(
              "Attribute is single-valued, set accepts one value: " + attribute);
        }
        change.set(set.values());
      }
      case ConfigOperation.Unset ignored -> change.unset();
      case ConfigOperation.Add add -> {
        requireMultiValued(attribute, multiValued);
        change.add(add.values());
      }
      case ConfigOperation.Remove remove -> {
        requireMultiValued(attribute, multiValued);
        change.remove(remove.values());
      }
    }
  }

  private static void requireMultiValued(String attribute, boolean multiValued) {
    if (!multiValued) {
      throw new InvalidConfigModificationException("Attribute is single-valued, use set or unset: " + attribute);
    }
  }

  private static String key(String attribute) {
    return attribute.toLowerCase(Locale.ROOT);
  }
}
