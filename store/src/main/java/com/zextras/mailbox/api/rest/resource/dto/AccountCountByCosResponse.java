/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource.dto;

import java.util.Map;

/**
 * Accounts per COS, and their sum.
 *
 * @param total   accounts across every COS asked about
 * @param byCos   accounts per COS id, keyed as the caller spelled them; a COS with no accounts is
 *                reported as zero rather than left out
 */
public record AccountCountByCosResponse(long total, Map<String, Long> byCos) {

}
