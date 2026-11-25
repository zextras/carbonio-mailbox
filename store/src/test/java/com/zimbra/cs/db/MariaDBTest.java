/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.db.MariaDBConfig;
import java.util.List;
import org.junit.jupiter.api.Test;

class MariaDBTest {

  private final MariaDB mariaDB = new MariaDB();

  @Test
  void upsertUpdatesGivenColumnsOnDuplicateKey() {
    String sql =
        mariaDB.upsert(
            "mboxgroup1.open_conversation",
            List.of("mailbox_id", "hash", "conv_id"),
            List.of("mailbox_id", "hash"),
            List.of("conv_id"));

    assertEquals(
        "INSERT INTO mboxgroup1.open_conversation (mailbox_id, hash, conv_id) VALUES (?, ?, ?)"
            + " ON DUPLICATE KEY UPDATE conv_id = VALUES(conv_id)",
        sql);
  }

  @Test
  void upsertWithoutUpdateColumnsKeepsExistingRow() {
    String sql =
        mariaDB.upsert(
            "mboxgroup1.tagged_item",
            List.of("mailbox_id", "tag_id", "item_id"),
            List.of("mailbox_id", "tag_id", "item_id"),
            List.of());

    assertEquals(
        "INSERT INTO mboxgroup1.tagged_item (mailbox_id, tag_id, item_id) VALUES (?, ?, ?)"
            + " ON DUPLICATE KEY UPDATE mailbox_id = mailbox_id",
        sql);
  }

  @Test
  void connectionsUseReadCommittedFromTheStart() {
    Object sessionVariables = new DbPool.PoolConfig(MariaDBConfig.fromLocalConfig()).mDatabaseProperties.get("sessionVariables");

    assertEquals("tx_isolation='READ-COMMITTED'", sessionVariables);
  }
}
