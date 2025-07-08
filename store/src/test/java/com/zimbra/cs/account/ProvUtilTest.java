/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import com.zimbra.cs.UsageException;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProvUtilTest {

	@Test
	void usageWithException_shouldThrowUsageException() {
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();

		final Console console = new Console(stdout, stderr);

		Assertions.assertThrows(UsageException.class, () -> new ProvUtil(console, Map.of()).usageWithException());
	}
}