/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import com.zextras.mailbox.testcontainers.MariaDBExtension;
import com.zextras.mailbox.testcontainers.MariaDBResource;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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

public final class TestMariaDB {

  private static final List<String> ZIMBRA_TABLES =
      List.of(
          "mailbox",
          "current_volumes",
          "deleted_account",
          "mailbox_metadata",
          "out_of_office",
          "config",
          "table_maintenance",
          "service_status",
          "scheduled_task",
          "pending_acl_push",
          "volume_blobs");

  private TestMariaDB() {}

  public static Db start(String volumeDirectory) throws Exception {
    MariaDBResource resource = MariaDBExtension.sharedResource();
    LC.mysql_bind_address.setDefault(resource.getHost());
    LC.mysql_port.setDefault(resource.getPort());
    LC.zimbra_mysql_user.setDefault("root");
    LC.zimbra_mysql_password.setDefault(MariaDBResource.ROOT_PASSWORD);
    try (Connection conn = connect()) {
      if (!schemaExists(conn, "zimbra")) {
        runScript(conn, "db.sql", Map.of());
        runScript(conn, "create_database.sql", Map.of("DATABASE_NAME", groupDatabase()));
        runScript(conn, "versions-init.sql", Map.of());
      }
      LC.create_db_sql_file.setDefault(copyToTempFile("create_database.sql").toString());
      try (PreparedStatement stmt =
          conn.prepareStatement("UPDATE zimbra.volume SET path = ? WHERE id = ?")) {
        stmt.setString(1, volumeDirectory + "/store");
        stmt.setInt(2, 1);
        stmt.executeUpdate();
        stmt.setString(1, volumeDirectory + "/index");
        stmt.setInt(2, 2);
        stmt.executeUpdate();
      }
    }
    return new MariaDB();
  }

  public static void clear() throws Exception {
    try (Connection conn = connect();
        Statement stmt = conn.createStatement()) {
      stmt.execute("SET foreign_key_checks = 0");
      for (String table : groupTables(conn)) {
        stmt.execute("DELETE FROM " + table);
      }
      for (String table : ZIMBRA_TABLES) {
        stmt.execute("DELETE FROM zimbra." + table);
      }
      stmt.execute(
          "INSERT INTO zimbra.current_volumes (message_volume_id, index_volume_id, next_mailbox_id)"
              + " VALUES (1, 2, 1)");
      stmt.execute("SET foreign_key_checks = 1");
    }
  }

  private static List<String> groupTables(Connection conn) throws SQLException {
    List<String> tables = new ArrayList<>();
    try (Statement stmt = conn.createStatement();
        ResultSet rs =
            stmt.executeQuery(
                "SELECT table_schema, table_name FROM information_schema.tables"
                    + " WHERE table_schema LIKE 'mboxgroup%'")) {
      while (rs.next()) {
        tables.add(rs.getString(1) + "." + rs.getString(2));
      }
    }
    return tables;
  }

  private static Path copyToTempFile(String file) throws IOException {
    Path copy = Files.createTempFile("mariadb-" + file, ".sql");
    copy.toFile().deleteOnExit();
    Files.writeString(copy, readScript(file));
    return copy;
  }

  private static String readScript(String file) throws IOException {
    try (InputStream in = TestMariaDB.class.getResourceAsStream("/db/" + file)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static String groupDatabase() {
    return DbMailbox.getDatabaseName(1);
  }

  private static Connection connect() throws SQLException {
    MariaDBResource resource = MariaDBExtension.sharedResource();
    return DriverManager.getConnection(
        "jdbc:mariadb://" + resource.getHost() + ":" + resource.getPort() + "/",
        "root",
        MariaDBResource.ROOT_PASSWORD);
  }

  private static boolean schemaExists(Connection conn, String schema) throws SQLException {
    try (PreparedStatement stmt =
        conn.prepareStatement("SELECT 1 FROM information_schema.schemata WHERE schema_name = ?")) {
      stmt.setString(1, schema);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  private static void runScript(Connection conn, String file, Map<String, String> vars)
      throws Exception {
    String script = StringUtil.fillTemplate(readScript(file), vars);
    try (Statement stmt = conn.createStatement()) {
      for (String sql : DbMailbox.parseScript(new StringReader(script), new MariaDB())) {
        if (!sql.isEmpty()) {
          stmt.execute(sql);
        }
      }
    }
  }
}
