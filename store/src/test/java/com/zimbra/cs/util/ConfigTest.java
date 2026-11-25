/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.util;

import com.zimbra.cs.db.DbConfig;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.TestMariaDB;
import java.io.File;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("e2e")
class ConfigTest {

  @TempDir private static File tempDir;

  @BeforeAll
  static void setUp() throws Exception {
    DbPool.global(TestMariaDB.start(tempDir.getAbsolutePath()));
  }

  @AfterAll
  static void tearDown() throws Exception {
    DbPool.shutDownAndClear();
  }

  @Test
  void initLoadsRowsWithAndWithoutModifiedTimestamp() throws Exception {
    // Production seeds the version rows without a modified timestamp (store/db/versions-init.sql),
    // while rows written at runtime, like purge.lastMailboxId, carry one.
    DbConnection conn = DbPool.getConnection();
    try {
      conn.prepareStatement("UPDATE config SET modified = NULL").executeUpdate();
      DbConfig.set(conn, Config.KEY_PURGE_LAST_MAILBOX_ID, "42");
      conn.commit();
    } finally {
      DbPool.quietClose(conn);
    }

    Config.init();

    Assertions.assertEquals("111", Config.getString("db.version", null));
    Assertions.assertEquals(42, Config.getInt(Config.KEY_PURGE_LAST_MAILBOX_ID, 0));
  }
}
