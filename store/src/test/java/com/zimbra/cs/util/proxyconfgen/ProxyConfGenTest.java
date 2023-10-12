package com.zimbra.cs.util.proxyconfgen;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.MailboxTestUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProxyConfGenTest {

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
  }

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
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

  @Plugin(name = "TestAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
  public static class TestAppender extends AbstractAppender {

    private final List<String> list = new ArrayList<>();

    @PluginFactory
    public static TestAppender createAppender(@PluginAttribute("name") String name) {
      return new TestAppender(name);
    }

    protected TestAppender(String name) {
      super(name, null, null);
    }

    @Override
    public void append(LogEvent event) {
      list.add(event.getMessage().getFormattedMessage());
    }

    public List<String> getLogs() {
      return list;
    }

    @Override
    public void stop() {
      System.out.println(list);
    }
  }
}
