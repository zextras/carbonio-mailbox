package com.zimbra.cs.util.proxyconfgen;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.io.File;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProxyConfGenTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void shouldUseOverrideTemplatePathWhenOverrideConditionMatches() {
    File exampleTemplateFile = new File(ProxyConfGen.mTemplateDir + "/nginx.conf.web.http.default");
    final String overrideTemplateFilePath =
        ProxyConfGen.OVERRIDE_TEMPLATE_DIR + File.separator + exampleTemplateFile.getName();
    final String templateFilePath =
        ProxyConfGen.getTemplateFilePath(exampleTemplateFile, overrideTemplateFilePath, true);
    assertEquals(
        ProxyConfGen.OVERRIDE_TEMPLATE_DIR + "/nginx.conf.web.http.default", templateFilePath);
  }

  @Test
  void shouldUseDefaultTemplatePathWhenOverrideConditionDoNotMatches() {
    File exampleTemplateFile = new File(ProxyConfGen.mTemplateDir + "/nginx.conf.web.http.default");
    final String overrideTemplateFilePath =
        ProxyConfGen.OVERRIDE_TEMPLATE_DIR + File.separator + exampleTemplateFile.getName();
    final String templateFilePath =
        ProxyConfGen.getTemplateFilePath(exampleTemplateFile, overrideTemplateFilePath, false);
    assertEquals(ProxyConfGen.mTemplateDir + "/nginx.conf.web.http.default", templateFilePath);
  }

  @Test
  void shouldLogInfoByDefault() throws Exception {
    catchSystemExit(() -> ProxyConfGen.main(new String[] {""}));
    final Logger rootLogger = org.apache.logging.log4j.LogManager.getRootLogger();
    Assertions.assertEquals(Level.INFO, rootLogger.getLevel());
  }

  @Test
  void shouldLogDebugWhenVerbose() throws Exception {
    catchSystemExit(() -> ProxyConfGen.main(new String[] {"-v"}));
    final Logger rootLogger = org.apache.logging.log4j.LogManager.getRootLogger();
    Assertions.assertEquals(Level.DEBUG, rootLogger.getLevel());
  }
}
