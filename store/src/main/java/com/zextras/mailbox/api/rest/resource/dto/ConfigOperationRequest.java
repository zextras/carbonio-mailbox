/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ConfigOperationRequest(
    @Schema(description = "Global config attribute name") String attribute,
    @Schema(description = "Operation", allowableValues = {"set", "unset", "add", "remove"}) String op,
    @Schema(description = "Values for set, add and remove; absent for unset") List<String> values) {

}
