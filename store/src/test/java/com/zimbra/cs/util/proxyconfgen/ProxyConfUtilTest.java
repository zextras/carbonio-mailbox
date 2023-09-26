package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.util.proxyconfgen.ProxyConfVar.KeyValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProxyConfUtilTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://www.test.com",
        "http://subdomain.test.com",
        "https://sub.domain.test.com:8080",
        "https://*.test.com",
        "https://*.test.com/*",
        "https://test.com/*",
        "https://test.com:8080/*",
        "https://test.com/path?param=value",
        "https://www.test.com:99999"
      })
  void isValidSrcDirectiveUrl_should_returnTrueForValidUrls(String url) {
    assertTrue(ProxyConfUtil.isValidSrcDirectiveUrl(url));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid_url",
        "http:/test.com",
        "https:/test.com",
        "https://www.test.com,https://test.com",
        "https://www.test.com https://test.com",
        "https://www.test.com;https://test.com",
        "https://www.test.com:abc"
      })
  void isValidSrcDirectiveUrl_should_returnFalseForInvalidUrls(String url) {
    assertFalse(ProxyConfUtil.isValidSrcDirectiveUrl(url));
  }

  @Test
  void parseHeaderLine_should_parseHeaderCorrectly() {
    final String headerLine1 = "Content-Security-Policy: default-src 'self';";
    final String headerLine2 = "X-Frame-Options: DENY";

    final KeyValue header1 = ProxyConfUtil.parseHeaderLine(headerLine1);
    final KeyValue header2 = ProxyConfUtil.parseHeaderLine(headerLine2);

    assertEquals("Content-Security-Policy", header1.key);
    assertEquals("default-src 'self';", header1.value);

    assertEquals("X-Frame-Options", header2.key);
    assertEquals("DENY", header2.value);
  }

  @Test
  void extractConnectSrcDirective_should_extractCorrectDirective() {
    final String cspHeader1 = "default-src 'self'; script-src 'self';";
    final String cspHeader2 =
        "default-src 'self'; connect-src 'self' https://test.com/login https://test.com/logout;";

    final String connectSrcDirective1 = ProxyConfUtil.extractConnectSrcDirectiveFromCSP(cspHeader1);
    final String connectSrcDirective2 = ProxyConfUtil.extractConnectSrcDirectiveFromCSP(cspHeader2);

    assertEquals("", connectSrcDirective1); // No connect-src directive in the header
    assertEquals(
        "connect-src 'self' https://test.com/login https://test.com/logout", connectSrcDirective2);
  }
}
