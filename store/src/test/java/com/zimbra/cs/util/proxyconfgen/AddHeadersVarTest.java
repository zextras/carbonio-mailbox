package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

class AddHeadersVarTest {

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

  @Test
  void testUpdateCSPHeader() throws ServiceException, ProxyConfException {
    String key = "web.add.headers.vhost";
    ArrayList<String> rhdr = new ArrayList<>();
    rhdr.add(
        "Content-Security-Policy: default-src 'self'; connect-src 'self' *.blahblah.tools;"
            + " script-src 'self';");
    String description = "add_header directive for vhost web proxy";

    Map<String, String> customLogInLogoutUrlValueMap = new HashMap<>();
    customLogInLogoutUrlValueMap.put(
        ZAttrProvisioning.A_carbonioWebUILoginURL, "https://auth.test.com");
    customLogInLogoutUrlValueMap.put(
        ZAttrProvisioning.A_carbonioWebUILogoutURL, "https://auth.test.com/logout");

    // Create an instance of AddHeadersVar
    AddHeadersVar addHeadersVar =
        new AddHeadersVar(key, rhdr, description, customLogInLogoutUrlValueMap);

    // Perform the update
    addHeadersVar.update();

    // test if the connect-src directive of CSP header was updated as expected
    String expectedCspValue =
        "add_header Content-Security-Policy default-src 'self'; connect-src 'self' *.blahblah.tools"
            + " https://auth.test.com https://auth.test.com/logout; script-src 'self';;";
    assertEquals(expectedCspValue, addHeadersVar.confValue());
  }

  @Test
  void testParseHeaderLine() {
    // Sample input data
    String headerLine1 = "Content-Security-Policy: default-src 'self';";
    String headerLine2 = "X-Frame-Options: DENY";

    // Test the parseHeaderLine method
    AddHeadersVar addHeadersVar = new AddHeadersVar(null, null, null, null);
    KeyValue header1 = addHeadersVar.parseHeaderLine(headerLine1);
    KeyValue header2 = addHeadersVar.parseHeaderLine(headerLine2);

    // Verify the results
    assertEquals("Content-Security-Policy", header1.key);
    assertEquals("default-src 'self';", header1.value);

    assertEquals("X-Frame-Options", header2.key);
    assertEquals("DENY", header2.value);
  }

  @Test
  void testModifyCspHeaderValue() {
    // Sample input data
    String cspValue1 = "default-src 'self'; script-src 'self';";
    String cspValue2 = "default-src 'self'; script-src 'self'; connect-src 'self';";

    // Test the modifyCspHeaderValue method
    AddHeadersVar addHeadersVar = new AddHeadersVar(null, null, null, null);
    String modifiedCspValue1 = addHeadersVar.modifyCspHeaderValue(cspValue1);
    String modifiedCspValue2 = addHeadersVar.modifyCspHeaderValue(cspValue2);

    // Verify the results
    assertEquals("", modifiedCspValue1); // No connect-src directive, so no modification
    assertEquals("default-src 'self'; script-src 'self'; connect-src 'self';", modifiedCspValue2);
  }

  @Test
  void testExtractConnectSrcDirective() {
    // Sample input data
    String cspHeader1 = "default-src 'self'; script-src 'self';";
    String cspHeader2 =
        "default-src 'self'; connect-src 'self' https://test.com/login https://test.com/logout;";

    // Test the extractConnectSrcDirective method
    AddHeadersVar addHeadersVar = new AddHeadersVar(null, null, null, null);
    String connectSrcDirective1 = addHeadersVar.extractConnectSrcDirective(cspHeader1);
    String connectSrcDirective2 = addHeadersVar.extractConnectSrcDirective(cspHeader2);

    // Verify the results
    assertEquals("", connectSrcDirective1); // No connect-src directive in the header
    assertEquals(
        "connect-src 'self' https://test.com/login https://test.com/logout", connectSrcDirective2);
  }
}
