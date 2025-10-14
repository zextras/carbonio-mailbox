/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import com.zextras.mailbox.MailboxTestSuite;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("special")
class VersionsTest extends MailboxTestSuite {

	@Test
	void testDBVersionMatches() {
		Assertions.assertTrue(Versions.checkDBVersion());
	}

	@Test
	void testIndexVersionMatches() {
		Assertions.assertTrue(Versions.checkIndexVersion());
	}

	@Test
	void testCmdLineDoesNotThrow_IfEmptyString() {
		Assertions.assertDoesNotThrow(() -> Versions.parseCmdlineArgs(new String[]{""}, new Options()));
	}

	@Test
	void testCmdLineThrows_IfOptionsNull() {
		Assertions.assertThrows(NullPointerException.class, () -> Versions.parseCmdlineArgs(new String[]{""}, null));
	}

}