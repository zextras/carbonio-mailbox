/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.internalapi.client;

import java.util.Map;

public record Account(String id, String name, String displayName, String cosId, String domainId,
    String domain, String status, boolean isGlobalAdmin, boolean isExternal, String locale,
    Map<String, Boolean> features, Map<String, String> capabilities, Long sessionLifetimeMs,
    boolean isExternalVirtualAccount) {

}
