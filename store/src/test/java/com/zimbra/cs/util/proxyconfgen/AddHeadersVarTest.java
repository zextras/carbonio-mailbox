package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
}
