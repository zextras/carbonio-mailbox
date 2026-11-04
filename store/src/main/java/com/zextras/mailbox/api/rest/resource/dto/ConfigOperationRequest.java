/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource.dto;

import com.zextras.mailbox.api.rest.service.ConfigOperation;
import com.zextras.mailbox.api.rest.service.InvalidConfigModificationException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ConfigOperationRequest(
    @Schema(description = "Global config attribute name") String attribute,
    @Schema(description = "Operation", allowableValues = {"set", "unset", "add", "remove"}) String op,
    @Schema(description = "Values for set, add and remove; absent for unset") List<String> values) {

  public ConfigOperation toOperation() {
    if (attribute == null || attribute.isBlank()) {
      throw new InvalidConfigModificationException("Missing attribute in operation");
    }
    return switch (op == null ? "" : op) {
      case "set" -> new ConfigOperation.Set(attribute, requiredValues());
      case "unset" -> unset();
      case "add" -> new ConfigOperation.Add(attribute, requiredValues());
      case "remove" -> new ConfigOperation.Remove(attribute, requiredValues());
      default -> throw new InvalidConfigModificationException(
          "Unknown operation '" + op + "', expected set, unset, add or remove");
    };
  }

  private ConfigOperation unset() {
    if (values != null) {
      throw new InvalidConfigModificationException("unset takes no values: " + attribute);
    }
    return new ConfigOperation.Unset(attribute);
  }

  private List<String> requiredValues() {
    if (values == null || values.isEmpty() || values.stream().anyMatch(value -> value == null || value.isBlank())) {
      throw new InvalidConfigModificationException("Missing or empty value for attribute: " + attribute);
    }
    return values;
  }
}
