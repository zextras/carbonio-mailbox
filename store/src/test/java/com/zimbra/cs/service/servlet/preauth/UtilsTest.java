package com.zimbra.cs.service.servlet.preauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UtilsTest {

  private static final String TEST_ACCOUNT_NAME = "test@account.com";
  private static final String TEST_ADMIN_ACCOUNT_NAME = "admin@account.com";

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount(TEST_ACCOUNT_NAME, "secret", attrs);

    Map<String, Object> adminAttrs = Maps.newHashMap();
    adminAttrs.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
    prov.createAccount(TEST_ADMIN_ACCOUNT_NAME, "secret", adminAttrs);
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void testGetRequiredParam() throws ServiceException {
    String paramName = "requiredParam";
    HttpServletRequest request = createMockRequestWithParamValue(paramName, "requiredValue");

    String result = Utils.getRequiredParam(request, paramName);
    assertEquals("requiredValue", result);
  }

  @Test
  void testMissingRequiredParam() {
    String paramName = "requiredParam";
    HttpServletRequest request = createMockRequestWithParamValue(paramName, null);

    assertThrows(ServiceException.class, () -> Utils.getRequiredParam(request, paramName));
  }

  @Test
  void testGetOptionalParamWithValue() {
    String paramName = "optionalParam";
    HttpServletRequest request = createMockRequestWithParamValue(paramName, "optionalValue");

    String result = Utils.getOptionalParam(request, paramName, "defaultValue");
    assertEquals("optionalValue", result);
  }

  @Test
  void testOptionalParamWithoutValue() {
    String paramName = "nonExistentParam";
    HttpServletRequest request = createMockRequestWithParamValue(paramName, null);

    String result = Utils.getOptionalParam(request, paramName, null);
    assertNull(result);
  }

  @Test
  void testOptionalParamWithDefaultValue() {
    String paramName = "nonExistentParam";
    HttpServletRequest request = createMockRequestWithParamValue(paramName, null);

    String result = Utils.getOptionalParam(request, paramName, "defaultValue");
    assertEquals("defaultValue", result);
  }

  @Test
  void testGetBaseUrl() {
    // Mock HttpServletRequest
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("test.com");

    // Test: getBaseUrl
    String baseUrl = Utils.getBaseUrl(request);

    assertEquals("http://test.com", baseUrl);
  }

  @Test
  void testConvertRedirectURLRelativeToContext() throws MalformedURLException {
    // Test: Valid URL with path parameter
    String inputURL1 = "http://test.com:8080/path";
    String expectedOutput1 = "/path";
    assertEquals(expectedOutput1, Utils.convertRedirectURLRelativeToContext(inputURL1));

    // Test: Valid URL with root as path parameter
    String inputURL2 = "https://sub.test.com/";
    String expectedOutput2 = "/";
    assertEquals(expectedOutput2, Utils.convertRedirectURLRelativeToContext(inputURL2));

    // Test: Invalid URL, should throw MalformedURLException
    String inputURL3 = "invalid-url";
    assertThrows(
        MalformedURLException.class,
        () -> {
          Utils.convertRedirectURLRelativeToContext(inputURL3);
        });

    // Test: Null input, should return null
    String inputURL4 = null;
    assertNull(Utils.convertRedirectURLRelativeToContext(inputURL4));
  }

  @Test
  void testGenerateAuthTokenForRegularAccount() throws ServiceException {
    // Test: regular account
    Account account = Provisioning.getInstance().get(Key.AccountBy.name, TEST_ACCOUNT_NAME);
    long expires = 1690231807995L;
    AuthToken expectedToken = AuthProvider.getAuthToken(account, expires, false, null);

    AuthToken authToken = Utils.generateAuthToken(account, expires, false);

    assertEquals(expectedToken.toString(), authToken.toString());
  }

  @Test
  void testGenerateAuthTokenForAdminAccount() throws ServiceException {
    // Test: admin account
    Account account = Provisioning.getInstance().get(Key.AccountBy.name, TEST_ADMIN_ACCOUNT_NAME);
    long expires = 1690231807995L;
    AuthToken expectedToken = AuthProvider.getAuthToken(account, expires, true, null);

    AuthToken authToken = Utils.generateAuthToken(account, expires, true);

    assertEquals(expectedToken.toString(), authToken.toString());
  }

  @Test
  void testCreateAuthContext() {
    // Prepare test data
    String accountIdentifier = "one@test.com";

    // Mock HttpServletRequest methods
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("User-Agent")).thenReturn("Test User Agent");
    when(request.getRemoteAddr()).thenReturn("192.168.1.100");
    when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 192.168.1.100");

    // Call the method to test
    Map<String, Object> authContext = Utils.createAuthContext(accountIdentifier, request);

    // Verify the expected behavior
    Map<String, Object> expectedAuthContext = new HashMap<>();
    expectedAuthContext.put(
        AuthContext.AC_ORIGINATING_CLIENT_IP,
        null); // Not able to get originating IP in test environment
    expectedAuthContext.put(AuthContext.AC_REMOTE_IP, "192.168.1.100");
    expectedAuthContext.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, "one@test.com");
    expectedAuthContext.put(AuthContext.AC_USER_AGENT, "Test User Agent");
    assertEquals(expectedAuthContext, authContext);
  }

  // Helper method to create an instance of the HttpServletRequest mock with provided parameter
  // value
  private HttpServletRequest createMockRequestWithParamValue(String paramName, String paramValue) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter(paramName)).thenReturn(paramValue);
    return request;
  }
}
