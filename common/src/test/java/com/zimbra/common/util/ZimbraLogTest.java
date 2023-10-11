// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.common.util;

import java.io.File;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ZimbraLogTest {

  @ParameterizedTest
  @ValueSource(strings = {"INFO", "ERROR", "WARN", "ALL", "DEBUG"})
  void shouldLogWithDefaultLevel(String level) {
    ZimbraLog.toolSetupLog4j(level, null, false);
    final ExtendedLogger test = LogManager.getContext(false).getLogger("test");
    Assertions.assertEquals(level, test.getLevel().toString());
  }

  @Test
  void shouldLogErrorIfLevelNull() {
    ZimbraLog.toolSetupLog4j(null, null, false);
    final ExtendedLogger test = LogManager.getContext(false).getLogger("test");
    Assertions.assertEquals(Level.ERROR, test.getLevel());
  }

  @Test
  void shouldLogErrorIfUnknownLevel() {
    ZimbraLog.toolSetupLog4j("ABRACADABRA", null, false);
    final ExtendedLogger test = LogManager.getContext(false).getLogger("test");
    Assertions.assertEquals(Level.ERROR, test.getLevel());
  }

  @ParameterizedTest
  @ValueSource(strings = {"INFO", "ERROR", "WARN", "ALL", "DEBUG"})
  @DisplayName(
      "Use log4j properties file with root level DEBUG. Check level matches independently from"
          + " default level.")
  void shouldLogUsingFileLevel(String defaultLevel) {
    final String file =
        Objects.requireNonNull(
            new File(this.getClass().getResource("log4j-test.properties").getFile())
                .getAbsolutePath());
    ZimbraLog.toolSetupLog4j(defaultLevel, file);
    final ExtendedLogger test = LogManager.getContext(false).getLogger("test");
    Assertions.assertEquals(Level.DEBUG, test.getLevel());
  }
}
