/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource.dto;

import java.util.List;
import java.util.Map;

public record ModifyConfigRequest(
    Map<String, List<String>> set,
    List<String> unset,
    Map<String, List<String>> add,
    Map<String, List<String>> remove) {

}
