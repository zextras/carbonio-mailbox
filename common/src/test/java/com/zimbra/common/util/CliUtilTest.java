// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CliUtilTest {

  @Test
  void shouldLogInfo() {
    CliUtil.toolSetup();
    final ExtendedLogger test = LogManager.getContext(false).getLogger("test");
    assertEquals(Level.INFO, test.getLevel());
  }

  @ParameterizedTest
  @ValueSource(strings = {"INFO", "ERROR", "WARN", "ALL", "DEBUG"})
  void shouldLogUsingZimbraLog4jLevelProperty(String level) {
    System.setProperty("zimbra.log4j.level", "WARN");
    CliUtil.toolSetup(level);
    final ExtendedLogger test = LogManager.getContext(false).getLogger("test");
    assertEquals(Level.WARN, test.getLevel());
  }

  @ParameterizedTest
  @ValueSource(strings = {"INFO", "ERROR", "WARN", "ALL", "DEBUG"})
  void shouldLogUsingGivenLevelWhenNoPropertyDefined(String level) {
    CliUtil.toolSetup(level);
    final ExtendedLogger test = LogManager.getContext(false).getLogger("test");
    assertEquals(level, test.getLevel().toString());
  }

  @ParameterizedTest
  @ValueSource(strings = {"INFO", "ERROR", "WARN", "ALL", "DEBUG"})
  void shouldLog(String level) {
    CliUtil.toolSetup(level, null, false);
    final ExtendedLogger test = LogManager.getContext(false).getLogger("test");
    assertEquals(level, test.getLevel().toString());
  }
}
