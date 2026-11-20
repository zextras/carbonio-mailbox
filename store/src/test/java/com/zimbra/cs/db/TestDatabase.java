// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

/**
 * Database backend for tests: in-memory HSQLDB by default, PostgreSQL in a container with {@code
 * -Dmailbox.test.db=postgresql}.
 */
public final class TestDatabase {

  private static final boolean POSTGRESQL =
      "postgresql".equalsIgnoreCase(System.getProperty("mailbox.test.db"));

  private TestDatabase() {}

  public static boolean isPostgreSQL() {
    return POSTGRESQL;
  }

  /** Makes the test backend the {@link Db} instance. */
  public static Db install() {
    Db db;
    if (POSTGRESQL) {
      PostgreSQLTestDatabase.start();
      db = new PostgreSQL();
    } else {
      db = new HSQLDB();
    }
    Db.setInstance(db);
    return db;
  }

  /** Creates the zimbra and mboxgroup1 schemas once per JVM. */
  public static void createDatabase(String volumeDirectory) throws Exception {
    if (POSTGRESQL) {
      PostgreSQLTestDatabase.createDatabase(volumeDirectory);
    } else {
      HSQLDB.createDatabase(volumeDirectory);
    }
  }

  /** Deletes all rows, keeping the volumes. */
  public static void clearDatabase() throws Exception {
    if (POSTGRESQL) {
      PostgreSQLTestDatabase.clearDatabase();
    } else {
      HSQLDB.clearDatabase();
    }
  }
}
