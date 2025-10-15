/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import com.zimbra.common.localconfig.LC;
import java.io.File;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VersionsTest {

	@TempDir
	private static File tempDir;

	@BeforeAll
	static void setUp() throws Exception {
		LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
		HSQLDB.createDatabase(tempDir.getAbsolutePath());
		DbPool.startup();
	}

	@AfterAll
	static void tearDown() throws Exception {
		DbPool.clear();
		DbPool.shutdown();
	}

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