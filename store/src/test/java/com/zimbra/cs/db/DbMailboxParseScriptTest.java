/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DbMailboxParseScriptTest {

  @Test
  void splitsStatementsAndRemovesWhitespaceAroundDelimiter() throws IOException {
    assertEquals(
        List.of("CREATE TABLE a (id INT)", "CREATE TABLE b (id INT)"),
        parse("CREATE TABLE a (id INT)  ;\n\n   CREATE TABLE b (id INT);\n"));
  }

  @Test
  void dropsCommentsAndBlankLines() throws IOException {
    assertEquals(
        List.of("CREATE TABLE a (id INT)"),
        parse("-- a comment\n# another comment\n\nCREATE TABLE a (id INT); -- trailing\n"));
  }

  @Test
  void keepsEndWithItsBlock() throws IOException {
    assertEquals(
        List.of("CREATE TRIGGER t BEGIN\nSET @a = 1;\nEND"),
        parse("CREATE TRIGGER t BEGIN\nSET @a = 1;\nEND;\n"));
  }

  @ParameterizedTest
  @CsvSource({"db.sql, 28", "create_database.sql, 24", "versions-init.sql, 3"})
  void schemaScriptSplitsIntoWholeStatements(String file, int expectedStatements)
      throws IOException {
    List<String> statements = parse(schemaScript(file));

    assertEquals(expectedStatements, statements.size());
    for (String statement : statements) {
      assertTrue(
          STATEMENT_START.matcher(statement).lookingAt(), "unexpected statement: " + statement);
    }
  }

  private static final Pattern STATEMENT_START =
      Pattern.compile(
          "(CREATE (DATABASE|TABLE|UNIQUE INDEX)|ALTER DATABASE|USE|INSERT INTO|GRANT|COMMIT)\\b",
          Pattern.CASE_INSENSITIVE);

  private static String schemaScript(String file) throws IOException {
    try (InputStream in = DbMailboxParseScriptTest.class.getResourceAsStream("/db/" + file)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static List<String> parse(String script) throws IOException {
    return Arrays.stream(DbMailbox.parseScript(new StringReader(script)))
        .filter(sql -> !sql.isEmpty())
        .toList();
  }
}
