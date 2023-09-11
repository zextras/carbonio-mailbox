package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AddHeadersVarTest {

  private Provisioning provisioning;

  @BeforeEach
  void setUp() {
    provisioning = mock(Provisioning.class);
  }

  @Test
  void update_should_modifyCspHeaderValueCorrectly() throws ServiceException, ProxyConfException {
    final String key = "web.add.headers.vhost";
    final ArrayList<String> responseHeaders = new ArrayList<>();
    responseHeaders.add(
        "Content-Security-Policy: default-src 'self'; connect-src 'self' *.test.tools;"
            + " script-src 'self';");
    final String description = "add_header directive for vhost web proxy";

    final List<String> customLogInLogoutUrlValueList = new ArrayList<>();
    customLogInLogoutUrlValueList.add("https://auth.test.com");
    customLogInLogoutUrlValueList.add("https://auth.test.com/logout");

    final AddHeadersVar addHeadersVar =
        new AddHeadersVar(
            provisioning, key, responseHeaders, description, customLogInLogoutUrlValueList);

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

    final List<String> customLogInLogoutUrlValueMap = new ArrayList<>();
    customLogInLogoutUrlValueMap.add("https://auth.test.com");
    customLogInLogoutUrlValueMap.add("https://auth.test.com/logout");
    customLogInLogoutUrlValueMap.add("https://admin-auth.test.com");
    customLogInLogoutUrlValueMap.add("https://admin-auth.test.com/logout");

    final AddHeadersVar addHeadersVar =
        new AddHeadersVar(
            provisioning, key, responseHeaders, description, customLogInLogoutUrlValueMap);

    addHeadersVar.update();

    final String expectedCspValue =
        "add_header Content-Security-Policy default-src 'self'; "
            + "connect-src 'self' *.test.tools https://auth.test.com https://auth.test.com/logout "
            + "https://admin-auth.test.com https://admin-auth.test.com/logout; script-src 'self';;";
    assertEquals(expectedCspValue, addHeadersVar.confValue());
  }
}
