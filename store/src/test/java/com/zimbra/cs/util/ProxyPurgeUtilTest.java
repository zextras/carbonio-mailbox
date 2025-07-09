package com.zimbra.cs.util;

import com.zimbra.common.cli.CommandExitException;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProxyPurgeUtilTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void shouldLogInfoByDefault()  {
    Assertions.assertThrows(CommandExitException.class, () -> ProxyPurgeUtil.run(new String[] {""}));
    final Logger rootLogger = org.apache.logging.log4j.LogManager.getRootLogger();
    Assertions.assertEquals(Level.INFO, rootLogger.getLevel());
  }

  @Test
  void shouldLogDebugWhenVerbose() {
    Assertions.assertThrows(CommandExitException.class, () -> ProxyPurgeUtil.run(new String[] {"-v"}));
    final Logger rootLogger = org.apache.logging.log4j.LogManager.getRootLogger();
    Assertions.assertEquals(Level.DEBUG, rootLogger.getLevel());
  }

}