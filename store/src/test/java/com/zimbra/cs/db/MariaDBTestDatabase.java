/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import com.zextras.mailbox.testcontainers.MariaDBExtension;
import com.zextras.mailbox.testcontainers.MariaDBResource;
import com.zimbra.common.util.StringUtil;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class MariaDBTestDatabase {

  static final MariaDB DB = new MariaDB();
  static final String GROUP = "mboxgroup1";

  private MariaDBTestDatabase() {}

  static Connection connect(MariaDBExtension mariadb) throws SQLException {
    return DriverManager.getConnection(mariadb.jdbcUrl(), "root", MariaDBResource.ROOT_PASSWORD);
  }

  static void createSchema(MariaDBExtension mariadb) throws Exception {
    try (Connection c = connect(mariadb)) {
      if (!schemaExists(c, GROUP)) {
        runScript(c, "db.sql", Map.of());
        runScript(c, "create_database.sql", Map.of("DATABASE_NAME", GROUP));
      }
    }
  }

  static void emptyTables(Connection conn, List<String> tables) throws SQLException {
    try (Statement s = conn.createStatement()) {
      s.execute("SET foreign_key_checks = 0");
      for (String table : tables) {
        s.execute("DELETE FROM " + table);
      }
    }
  }

  static int execute(Connection conn, String sql, Object... values) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (int i = 0; i < values.length; i++) {
        stmt.setObject(i + 1, values[i]);
      }
      return stmt.executeUpdate();
    }
  }

  static List<String> rows(Connection conn, String sql, Object... values) throws SQLException {
    List<String> rows = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (int i = 0; i < values.length; i++) {
        stmt.setObject(i + 1, values[i]);
      }
      try (ResultSet rs = stmt.executeQuery()) {
        int columns = rs.getMetaData().getColumnCount();
        while (rs.next()) {
          List<String> row = new ArrayList<>();
          for (int i = 1; i <= columns; i++) {
            row.add(rs.getString(i));
          }
          rows.add(String.join("|", row));
        }
      }
    }
    return rows;
  }

  private static boolean schemaExists(Connection c, String schema) throws SQLException {
    return !rows(c, "SELECT 1 FROM information_schema.schemata WHERE schema_name = ?", schema).isEmpty();
  }

  private static void runScript(Connection c, String file, Map<String, String> vars) throws Exception {
    String script = StringUtil.fillTemplate(Files.readString(Path.of("db", file)), vars);
    try (Statement stmt = c.createStatement()) {
      for (String sql : DbUtil.parseScript(new StringReader(script), DB)) {
        if (!sql.isEmpty()) {
          stmt.execute(sql);
        }
      }
    }
  }
}
