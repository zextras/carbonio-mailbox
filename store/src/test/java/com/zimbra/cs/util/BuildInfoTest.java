/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BuildInfoTest {

	@Test
	void shouldRetrieveReleaseFromVersion() {
		Assertions.assertEquals("carbonio", BuildInfo.getVersion().release());
	}

}