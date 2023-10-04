package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LogFactoryTest {

  @Test
  void shouldCreateLoggerAndAddItToLogManager() {
    final String name = "name";
    final Log log = LogFactory.getLog(name);
    assertEquals(name, log.getCategory());
    assertTrue(LogManager.logExists(name));
  }
}
