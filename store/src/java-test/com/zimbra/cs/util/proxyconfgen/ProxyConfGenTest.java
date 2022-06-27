package com.zimbra.cs.util.proxyconfgen;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.junit.Test;

public class ProxyConfGenTest {

  @Test
  public void shouldUseOverrideTemplatePathWhenOverrideConditionMatches() {
    File exampleTemplateFile = new File(ProxyConfGen.mTemplateDir + "/nginx.conf.web.http.default");
    final String overrideTemplateFilePath =
        ProxyConfGen.OVERRIDE_TEMPLATE_DIR + File.separator + exampleTemplateFile.getName();
    final String templateFilePath =
        ProxyConfGen.getTemplateFilePath(exampleTemplateFile, overrideTemplateFilePath, true);
    assertEquals(
        ProxyConfGen.OVERRIDE_TEMPLATE_DIR + "/nginx.conf.web.http.default", templateFilePath);
  }

  @Test
  public void shouldUseDefaultTemplatePathWhenOverrideConditionDoNotMatches() {
    File exampleTemplateFile = new File(ProxyConfGen.mTemplateDir + "/nginx.conf.web.http.default");
    final String overrideTemplateFilePath =
        ProxyConfGen.OVERRIDE_TEMPLATE_DIR + File.separator + exampleTemplateFile.getName();
    final String templateFilePath =
        ProxyConfGen.getTemplateFilePath(exampleTemplateFile, overrideTemplateFilePath, false);
    assertEquals(ProxyConfGen.mTemplateDir + "/nginx.conf.web.http.default", templateFilePath);
  }
}
