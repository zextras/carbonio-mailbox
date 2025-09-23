package com.zimbra.cs.util;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.cli.CommandExitException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProxyPurgeUtilTest extends MailboxTestSuite {

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