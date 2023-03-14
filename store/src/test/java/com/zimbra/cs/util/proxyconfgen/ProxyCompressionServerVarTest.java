package com.zimbra.cs.util.proxyconfgen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.MockServer;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProxyCompressionServerVarTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @After
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shouldUpdateProxyConfGenVarValueWhenCalledUpdate() throws Exception {

    // setup
    Entry mockServerSource = new MockServer("server", "0");

    Map<String, Object> attrs = new HashMap<>();
    attrs.put(ZAttrProvisioning.A_zimbraHttpCompressionEnabled, true);
    mockServerSource.setAttrs(attrs);

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
}