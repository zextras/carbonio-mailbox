/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import static com.zimbra.cs.db.MariaDBTestDatabase.GROUP;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.db.Db;
import com.zextras.db.DbError;
import com.zextras.db.MariaDB;
import com.zextras.mailbox.testcontainers.MariaDBExtension;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MariaDBErrorTest {

  @RegisterExtension static final MariaDBExtension mariadb = new MariaDBExtension();

  private static final String FULL_TABLE = GROUP + ".full_table";

  private final Db db = MariaDB.fromLocalConfig();
  private Connection conn;

  @BeforeAll
  static void createSchema() throws Exception {
    MariaDBTestDatabase.createSchema(mariadb);
  }

  @BeforeEach
  void connect() throws SQLException {
    conn = MariaDBTestDatabase.connect(mariadb);
    conn.setAutoCommit(false);
  }

  @AfterEach
  void close() throws SQLException {
    conn.rollback();
    conn.close();
  }

  @Test
  void insertingSameRowTwiceIsDuplicateRow() throws SQLException {
    configEntry("error-test");

    SQLException e = assertThrows(SQLException.class, () -> configEntry("error-test"));

    assertTrue(db.matches(e, DbError.DUPLICATE_ROW));
  }

  @Test
  void deletingReferencedVolumeIsForeignKeyChildExists() throws SQLException {
    execute(
        "INSERT INTO zimbra.current_volumes (message_volume_id, index_volume_id, next_mailbox_id)"
            + " VALUES (1, 2, 1)");

    SQLException e =
        assertThrows(SQLException.class, () -> execute("DELETE FROM zimbra.volume WHERE id = 1"));

    assertTrue(db.matches(e, DbError.FOREIGN_KEY_CHILD_EXISTS));
  }

  @Test
  void selectingMissingTableIsNoSuchTable() {
    SQLException e =
        assertThrows(
            SQLException.class, () -> execute("DELETE FROM " + GROUP + ".no_such_table"));

    assertTrue(db.matches(e, DbError.NO_SUCH_TABLE));
  }

  @Test
  void fillingMemoryTableIsTableFull() throws SQLException {
    execute("SET SESSION max_heap_table_size = 16384");
    execute("CREATE TEMPORARY TABLE " + FULL_TABLE + " (c CHAR(255)) ENGINE = MEMORY");

    SQLException e =
        assertThrows(
            SQLException.class,
            () -> {
              for (int i = 0; i < 10_000; i++) {
                execute("INSERT INTO " + FULL_TABLE + " VALUES ('x')");
              }
            });

    assertTrue(db.matches(e, DbError.TABLE_FULL));
  }

  @Test
  void duplicateRowDoesNotMatchOtherErrors() throws SQLException {
    configEntry("error-test");

    SQLException e = assertThrows(SQLException.class, () -> configEntry("error-test"));

    assertFalse(db.matches(e, DbError.FOREIGN_KEY_CHILD_EXISTS));
    assertFalse(db.matches(e, DbError.NO_SUCH_TABLE));
    assertFalse(db.matches(e, DbError.TABLE_FULL));
  }

  private void configEntry(String name) throws SQLException {
    execute("INSERT INTO zimbra.config (name, value) VALUES (?, 'value')", name);
  }

  private void execute(String sql, Object... values) throws SQLException {
    MariaDBTestDatabase.execute(conn, sql, values);
  }
}
