package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProxyCompressionServerVarTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

 /**
  * test default behavior when {@code ZAttrProvisioning.A_zimbraHttpCompressionEnabled} is set to
  * true (default) returned value must contain defined directive definition
  *
  * @throws Exception if any
  */
 @Test
 void shouldUpdateProxyConfGenVarValueWhenCalledUpdate() throws Exception {

  // setup
  Entry mockServerSource = mock(Server.class);

  // set up mock behavior to make A_zimbraHttpCompressionEnabled return true
  // we won't use mockito ArgumentMatcher.any* and use concrete possible values
  // to ensure method contract
  when(mockServerSource.getBooleanAttr(ZAttrProvisioning.A_zimbraHttpCompressionEnabled,
    true)).thenReturn(true);

  ProxyCompressionServerVar proxyCompressionServerVar = new ProxyCompressionServerVar();

  // set the mockServerSource on the ProxyCompressionServerVar instance
  ProxyConfVar.serverSource = mockServerSource;

  // execute: call the update method
  proxyCompressionServerVar.update();

  // expected value
  final String expectedVal = "\n"
    + "    gzip on;\n"
    + "    gzip_disable \"msie6\";\n"
    + "    gzip_vary on;\n"
    + "    gzip_proxied any;\n"
    + "    gzip_comp_level 6;\n"
    + "    gzip_buffers 16 8k;\n"
    + "    gzip_http_version 1.1;\n"
    + "    gzip_min_length 256;\n"
    + "    gzip_types\n"
    + "        application/atom+xml\n"
    + "        application/geo+json\n"
    + "        application/javascript\n"
    + "        application/x-javascript\n"
    + "        application/json\n"
    + "        application/ld+json\n"
    + "        application/manifest+json\n"
    + "        application/rdf+xml\n"
    + "        application/rss+xml\n"
    + "        application/xhtml+xml\n"
    + "        application/xml\n"
    + "        font/eot\n"
    + "        font/otf\n"
    + "        font/ttf\n"
    + "        font/woff2\n"
    + "        image/svg+xml\n"
    + "        text/css\n"
    + "        text/javascript\n"
    + "        text/plain\n"
    + "        text/xml;\n"
    + "\n"
    + "\n"
    + "    brotli on;\n"
    + "    brotli_static on;\n"
    + "    brotli_types\n"
    + "        application/atom+xml\n"
    + "        application/geo+json\n"
    + "        application/javascript\n"
    + "        application/x-javascript\n"
    + "        application/json\n"
    + "        application/ld+json\n"
    + "        application/manifest+json\n"
    + "        application/rdf+xml\n"
    + "        application/rss+xml\n"
    + "        application/xhtml+xml\n"
    + "        application/xml\n"
    + "        font/eot\n"
    + "        font/otf\n"
    + "        font/ttf\n"
    + "        font/woff2\n"
    + "        image/svg+xml\n"
    + "        text/css\n"
    + "        text/javascript\n"
    + "        text/plain\n"
    + "        text/xml;\n"
    + "\n";

  // verify: that the value is not null or empty
  assertNotNull(proxyCompressionServerVar.mValue);

  // verify: if the value is as expected
  assertEquals(expectedVal, proxyCompressionServerVar.mValue);
 }

 /**
  * test behavior when {@code ZAttrProvisioning.A_zimbraHttpCompressionEnabled} is set to false,
  * returned value must be empty
  *
  * @throws Exception if any
  */
 @Test
 void shouldReturnEmptyProxyConfGenVarValueWhenCompressionDisabledAndCalledUpdate()
   throws Exception {

  // setup
  Entry mockServerSource = mock(Server.class);

  // set up mock behavior to make A_zimbraHttpCompressionEnabled return false
  // we won't use mockito ArgumentMatcher.any* and use concrete possible values
  // to ensure method contract
  when(mockServerSource.getBooleanAttr(ZAttrProvisioning.A_zimbraHttpCompressionEnabled,
    true)).thenReturn(false);

  ProxyCompressionServerVar proxyCompressionServerVar = new ProxyCompressionServerVar();

  // set the mockServerSource on the ProxyCompressionServerVar instance
  ProxyConfVar.serverSource = mockServerSource;

  // execute: call the update method
  proxyCompressionServerVar.update();

  // verify: that the value is not null or empty
  assertNotNull(proxyCompressionServerVar.mValue);

  // verify: if the value is as expected
  assertEquals("", proxyCompressionServerVar.mValue);
 }
}