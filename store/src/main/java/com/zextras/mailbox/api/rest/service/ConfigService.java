/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCardinality;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Provisioning;
import io.vavr.control.Try;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigService {

  private final Supplier<Provisioning> provisioningSupplier;
  private final Supplier<AttributeManager> attributeManagerSupplier;

  public ConfigService(Supplier<Provisioning> provisioningSupplier, Supplier<AttributeManager> attributeManagerSupplier) {
    this.provisioningSupplier = provisioningSupplier;
    this.attributeManagerSupplier = attributeManagerSupplier;
  }

  public Try<Void> modifyConfig(List<ConfigOperation> operations) {
    return Try.run(() -> write(toModifications(operations)));
  }

  private void write(Map<String, Object> modifications) throws ServiceException {
    if (modifications.isEmpty()) {
      return;
    }
    final Provisioning provisioning = provisioningSupplier.get();
    provisioning.modifyAttrs(provisioning.getConfig(), modifications, true);
  }

  private Map<String, Object> toModifications(List<ConfigOperation> operations) {
    if (operations.isEmpty()) {
      throw new InvalidConfigModificationException("Nothing to modify: provide at least one operation");
    }
    final AttributeManager attributeManager = attributeManagerSupplier.get();
    return operations.stream()
        .map(operation -> checked(operation, attributeManager))
        .collect(Collectors.toMap(ConfigOperation::attribute, AttributeChange::of, AttributeChange::then, LinkedHashMap::new))
        .entrySet().stream()
        .flatMap(entry -> entry.getValue().modifications(entry.getKey()).entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static ConfigOperation checked(ConfigOperation operation, AttributeManager attributeManager) {
    final AttributeInfo info = Optional.ofNullable(attributeManager.getAttributeInfo(operation.attribute()))
        .orElseThrow(() -> new InvalidConfigModificationException("Unknown attribute: " + operation.attribute()));
    if (!isInGlobalConfig(info)) {
      throw new InvalidConfigModificationException("Attribute is not part of the global config: " + info.getName());
    }
    return checkCardinality(operation.withAttribute(info.getName()), info.getCardinality() == AttributeCardinality.multi);
  }

  private static boolean isInGlobalConfig(AttributeInfo info) {
    return Stream.of(info.getOptionalIn(), info.getRequiredIn())
        .filter(Objects::nonNull)
        .flatMap(Set::stream)
        .anyMatch(AttributeClass.globalConfig::equals);
  }

  private static ConfigOperation checkCardinality(ConfigOperation operation, boolean multiValued) {
    if (multiValued) {
      return operation;
    }
    return switch (operation) {
      case ConfigOperation.Set set when set.values().size() > 1 -> throw new InvalidConfigModificationException(
          "Attribute is single-valued, set accepts one value: " + set.attribute());
      case ConfigOperation.Add add -> throw new InvalidConfigModificationException(
          "Attribute is single-valued, use set or unset: " + add.attribute());
      case ConfigOperation.Remove remove -> throw new InvalidConfigModificationException(
          "Attribute is single-valued, use set or unset: " + remove.attribute());
      default -> operation;
    };
  }
}
