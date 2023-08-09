package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.util.proxyconfgen.ProxyConfVar.KeyValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AddHeadersVarTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void update_should_modifyCspHeaderValueCorrectly() throws ServiceException, ProxyConfException {
    final String key = "web.add.headers.vhost";
    final ArrayList<String> responseHeaders = new ArrayList<>();
    responseHeaders.add(
        "Content-Security-Policy: default-src 'self'; connect-src 'self' *.test.tools;"
            + " script-src 'self';");
    final String description = "add_header directive for vhost web proxy";

    final Map<String, String> customLogInLogoutUrlValueMap = new HashMap<>();
    customLogInLogoutUrlValueMap.put(
        ZAttrProvisioning.A_carbonioWebUILoginURL, "https://auth.test.com");
    customLogInLogoutUrlValueMap.put(
        ZAttrProvisioning.A_carbonioWebUILogoutURL, "https://auth.test.com/logout");

    final AddHeadersVar addHeadersVar =
        new AddHeadersVar(key, responseHeaders, description, customLogInLogoutUrlValueMap);

    addHeadersVar.update();

    final String expectedCspValue =
        "add_header Content-Security-Policy default-src 'self'; connect-src 'self' *.test.tools"
            + " https://auth.test.com https://auth.test.com/logout; script-src 'self';;";
    assertEquals(expectedCspValue, addHeadersVar.confValue());
  }

  @Test
  void parseHeaderLine_should_parseHeaderCorrectly() {
    final String headerLine1 = "Content-Security-Policy: default-src 'self';";
    final String headerLine2 = "X-Frame-Options: DENY";

    final AddHeadersVar addHeadersVar = new AddHeadersVar(null, null, null, null);
    final KeyValue header1 = addHeadersVar.parseHeaderLine(headerLine1);
    final KeyValue header2 = addHeadersVar.parseHeaderLine(headerLine2);

    assertEquals("Content-Security-Policy", header1.key);
    assertEquals("default-src 'self';", header1.value);

    assertEquals("X-Frame-Options", header2.key);
    assertEquals("DENY", header2.value);
  }

  @Test
  void modifyCspHeaderValue_should_modifyHeaderValueCorrectly() {
    final String cspValue1 = "default-src 'self'; script-src 'self';";
    final String cspValue2 = "default-src 'self'; script-src 'self'; connect-src 'self';";

    final AddHeadersVar addHeadersVar = new AddHeadersVar(null, null, null, null);
    final String modifiedCspValue1 = addHeadersVar.generateModifiedCspHeaderValue(cspValue1);
    final String modifiedCspValue2 = addHeadersVar.generateModifiedCspHeaderValue(cspValue2);

    assertEquals("", modifiedCspValue1); // No connect-src directive, so no modification
    assertEquals("default-src 'self'; script-src 'self'; connect-src 'self';", modifiedCspValue2);
  }

  @Test
  void update_should_modifyCspHeaderValueCorrectly_when_multipleUrls()
      throws ServiceException, ProxyConfException {
    final String key = "web.add.headers.default";
    final ArrayList<String> responseHeaders = new ArrayList<>();
    responseHeaders.add(
        "Content-Security-Policy: default-src 'self'; connect-src 'self' *.test.tools;"
            + " script-src 'self';");
    final String description = "add_header directive for vhost web proxy";

    final Map<String, String> customLogInLogoutUrlValueMap = new HashMap<>();
    customLogInLogoutUrlValueMap.put(
        ZAttrProvisioning.A_carbonioWebUILoginURL, "https://auth.test.com");
    customLogInLogoutUrlValueMap.put(
        ZAttrProvisioning.A_carbonioWebUILogoutURL, "https://auth.test.com/logout");
    customLogInLogoutUrlValueMap.put(
        ZAttrProvisioning.A_carbonioAdminUILoginURL, "https://admin-auth.test.com");
    customLogInLogoutUrlValueMap.put(
        ZAttrProvisioning.A_carbonioAdminUILogoutURL, "https://admin-auth.test.com/logout");

    final AddHeadersVar addHeadersVar =
        new AddHeadersVar(key, responseHeaders, description, customLogInLogoutUrlValueMap);

    addHeadersVar.update();

    final String expectedCspValue =
        "add_header Content-Security-Policy default-src 'self'; connect-src 'self'"
            + " *.test.tools https://auth.test.com https://admin-auth.test.com/logout"
            + " https://auth.test.com/logout https://admin-auth.test.com; script-src 'self';;";
    assertEquals(expectedCspValue, addHeadersVar.confValue());
  }

  @Test
  void extractConnectSrcDirective_should_extractCorrectDirective() {
    final String cspHeader1 = "default-src 'self'; script-src 'self';";
    final String cspHeader2 =
        "default-src 'self'; connect-src 'self' https://test.com/login https://test.com/logout;";

    final AddHeadersVar addHeadersVar = new AddHeadersVar(null, null, null, null);
    final String connectSrcDirective1 = addHeadersVar.extractConnectSrcDirective(cspHeader1);
    final String connectSrcDirective2 = addHeadersVar.extractConnectSrcDirective(cspHeader2);

    assertEquals("", connectSrcDirective1); // No connect-src directive in the header
    assertEquals(
        "connect-src 'self' https://test.com/login https://test.com/logout", connectSrcDirective2);
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
    assertTrue(AddHeadersVar.isValidSrcDirectiveUrl(url));
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
    assertFalse(AddHeadersVar.isValidSrcDirectiveUrl(url));
  }
}
