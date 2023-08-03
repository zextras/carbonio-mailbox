package com.zimbra.cs.service.servlet.preauth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UtilsTest {

  private static final String TEST_ACCOUNT_NAME = "test@account.com";
  private static final String TEST_ADMIN_ACCOUNT_NAME = "admin@account.com";

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();

    final Provisioning prov = Provisioning.getInstance();

    final Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount(TEST_ACCOUNT_NAME, "secret", attrs);

    final Map<String, Object> adminAttrs = Maps.newHashMap();
    adminAttrs.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
    prov.createAccount(TEST_ADMIN_ACCOUNT_NAME, "secret", adminAttrs);
  }

  @Test
  void getRequiredParam_should_returnValue_when_paramExists() throws ServiceException {
    final String paramName = "requiredParam";
    final HttpServletRequest request = createMockRequestWithParamValue(paramName, "requiredValue");

    final String result = Utils.getRequiredParam(request, paramName);
    assertEquals("requiredValue", result);
  }

  @Test
  void getRequiredParam_should_throwServiceException_when_paramMissing() {
    final String paramName = "requiredParam";
    final HttpServletRequest request = createMockRequestWithParamValue(paramName, null);

    assertThrows(ServiceException.class, () -> Utils.getRequiredParam(request, paramName));
  }

  @Test
  void getOptionalParam_should_returnValue_when_paramExists() {
    final String paramName = "optionalParam";
    final HttpServletRequest request = createMockRequestWithParamValue(paramName, "optionalValue");

    // Test: getOptionalParam returns correct param value when it is provided
    final Optional<String> result = Utils.getOptionalParam(request, paramName, "defaultValue");

    assertTrue(result.isPresent());
    assertThat(result.get(), is("optionalValue"));
  }

  @Test
  void getOptionalParam_should_returnEmptyOptional_when_paramMissingAndDefaultValueNull() {
    final String paramName = "nonExistentParam";
    final HttpServletRequest request = createMockRequestWithParamValue(paramName, null);

    // Test: getOptionalParam return null if param value is null and default value is also null
    final Optional<String> result = Utils.getOptionalParam(request, paramName, null);
    assertFalse(result.isPresent());
  }

  @Test
  void getOptionalParam_should_returnDefaultValue_when_paramMissingAndDefaultValueProvided() {
    final String paramName = "nonExistentParam";
    final HttpServletRequest request = createMockRequestWithParamValue(paramName, null);

    // Test: getOptionalParam returns default value if param value is null
    final Optional<String> result = Utils.getOptionalParam(request, paramName, "defaultValue");

    assertTrue(result.isPresent());
    assertThat(result.get(), is("defaultValue"));
  }

  @Test
  void getBaseUrl_should_returnBaseUrl_when_validRequest() {
    // Mock HttpServletRequest
    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("test.com");

    // Test: getBaseUrl returns correct value
    final String baseUrl = Utils.getBaseUrl(request);

    assertEquals("http://test.com", baseUrl);
  }

  @Test
  void convertRedirectURLRelativeToContext_should_returnConvertedUrl_when_validInput()
      throws MalformedURLException {
    // Test: Valid URL with path parameter
    final String inputURL1 = "http://test.com:8080/path";
    final String expectedOutput1 = "/path";
    assertEquals(expectedOutput1, Utils.convertRedirectURLRelativeToContext(inputURL1));

    // Test: Valid URL with root as path parameter
    final String inputURL2 = "https://sub.test.com/";
    final String expectedOutput2 = "/";
    assertEquals(expectedOutput2, Utils.convertRedirectURLRelativeToContext(inputURL2));
  }

  @Test
  void convertRedirectURLRelativeToContext_should_throwMalformedURLException_when_invalidInput() {
    // Test: Invalid URL, should throw MalformedURLException
    final String inputURL3 = "invalid-url";
    assertThrows(
        MalformedURLException.class, () -> Utils.convertRedirectURLRelativeToContext(inputURL3));
  }

  @Test
  void convertRedirectURLRelativeToContext_should_returnNull_when_inputIsNull()
      throws MalformedURLException {
    // Test: Null input, should return null
    assertNull(Utils.convertRedirectURLRelativeToContext(null));
  }

  @Test
  void generateAuthToken_should_generateAuthTokenForRegularAccount() throws ServiceException {
    final Account account = Provisioning.getInstance().get(Key.AccountBy.name, TEST_ACCOUNT_NAME);
    final long expires = 1690231807995L;
    final AuthToken expectedToken = AuthProvider.getAuthToken(account, expires, false, null);

    // Test: regular account
    final AuthToken authToken = Utils.generateAuthToken(account, expires, false);

    assertEquals(expectedToken.toString(), authToken.toString());
  }

  @Test
  void generateAuthToken_should_generateAuthTokenForAdminAccount() throws ServiceException {
    final Account account =
        Provisioning.getInstance().get(Key.AccountBy.name, TEST_ADMIN_ACCOUNT_NAME);
    final long expires = 1690231807995L;
    final AuthToken expectedToken = AuthProvider.getAuthToken(account, expires, true, null);

    // Test: admin account
    final AuthToken authToken = Utils.generateAuthToken(account, expires, true);

    assertEquals(expectedToken.toString(), authToken.toString());
  }

  @Test
  void createAuthContext_should_createValidAuthContextObject() {
    final String accountIdentifier = "one@test.com";

    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("User-Agent")).thenReturn("Test User Agent");
    when(request.getRemoteAddr()).thenReturn("192.168.1.100");
    when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 192.168.1.100");

    // Test: createAuthContext creates a valid auth context object
    final Map<String, Object> authContext = Utils.createAuthContext(accountIdentifier, request);

    final Map<String, Object> expectedAuthContext = new HashMap<>();
    expectedAuthContext.put(
        AuthContext.AC_ORIGINATING_CLIENT_IP,
        null); // Not able to get originating IP in test environment
    expectedAuthContext.put(AuthContext.AC_REMOTE_IP, "192.168.1.100");
    expectedAuthContext.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, "one@test.com");
    expectedAuthContext.put(AuthContext.AC_USER_AGENT, "Test User Agent");
    assertEquals(expectedAuthContext, authContext);
  }

  /**
   * Helper method to create an instance of the HttpServletRequest mock with provided parameter
   * value
   *
   * @param paramName the name of the parameter
   * @param paramValue the value of the parameter
   * @return mocked {@link HttpServletRequest} object with added parameters
   */
  private HttpServletRequest createMockRequestWithParamValue(String paramName, String paramValue) {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter(paramName)).thenReturn(paramValue);
    return request;
  }
}
