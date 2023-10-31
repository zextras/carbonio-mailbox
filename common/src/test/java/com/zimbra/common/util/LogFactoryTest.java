package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.util.Log.Level;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogFactoryTest {

  @Test
  void shouldCreateLoggerAndAddItToLogManager() {
    final String name = "name";
    final Log log = LogFactory.getLog(name);
    assertEquals(name, log.getCategory());
    assertTrue(LogManager.logExists(name));
  }

  @Test
  @DisplayName("Check without any log4j config the default level is error")
  void shouldLogWithLevelErrorByDefault() {
    final String name = "name";
    final Log log = LogFactory.getLog(name);
    assertEquals(Level.error, log.getLevel());
  }
}
