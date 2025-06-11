package com.zimbra.cs.imap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthTokenCacheHelperTest {

  private Account mockAccount;
  private AuthToken mockToken;

  @BeforeEach
  void setUp() {
    AuthTokenCacheHelper.clearCache();
    mockAccount = mock(Account.class);
    mockToken = mock(AuthToken.class);
    when(mockAccount.getId()).thenReturn("accountId");
  }

  @Test
  void returnsCachedValidAuthToken() throws Exception {
    Provisioning mockProvisioning = mock(Provisioning.class);

    try (MockedStatic<AuthProvider> authProvider = mockStatic(AuthProvider.class);
        MockedStatic<Provisioning> provisioning = mockStatic(Provisioning.class)) {
      provisioning.when(Provisioning::getInstance).thenReturn(mockProvisioning);
      authProvider.when(() -> AuthProvider.getAuthToken(mockAccount)).thenReturn(mockToken);
      authProvider
          .when(() -> AuthProvider.validateAuthToken(mockProvisioning, mockToken, true))
          .thenReturn(null);
      when(mockToken.getEncoded()).thenReturn("token-encoded");

      AuthToken token1 = AuthTokenCacheHelper.getValidAuthToken(mockAccount);
      AuthToken token2 = AuthTokenCacheHelper.getValidAuthToken(mockAccount);

      assertSame(token1, token2, "Should return the same cached AuthToken instance");
    }
  }

  @Test
  void generatesNewTokenWhenCachedTokenIsInvalid() throws Exception {
    AuthToken invalidToken = mock(AuthToken.class);
    AuthToken newToken = mock(AuthToken.class);
    Provisioning mockProvisioning = mock(Provisioning.class);

    try (MockedStatic<AuthProvider> authProvider = mockStatic(AuthProvider.class);
        MockedStatic<Provisioning> provisioning = mockStatic(Provisioning.class)) {
      provisioning.when(Provisioning::getInstance).thenReturn(mockProvisioning);
      authProvider
          .when(() -> AuthProvider.getAuthToken(mockAccount))
          .thenReturn(invalidToken, newToken);
      authProvider
          .when(() -> AuthProvider.validateAuthToken(mockProvisioning, invalidToken, true))
          .thenThrow(ServiceException.FAILURE("Invalid token", null));
      authProvider
          .when(() -> AuthProvider.validateAuthToken(mockProvisioning, newToken, true))
          .thenReturn(null);

      when(invalidToken.getEncoded()).thenReturn("invalid-token");
      when(newToken.getEncoded()).thenReturn("new-valid-token");

      AuthToken token = AuthTokenCacheHelper.getValidAuthToken(mockAccount);
      assertEquals("new-valid-token", token.getEncoded());
    }
  }

  @Test
  void throwsServiceExceptionWhenAuthProviderFails() throws Exception {
    Provisioning mockProvisioning = mock(Provisioning.class);
    AuthProviderException authError = AuthProviderException.FAILURE("Token generation failed");

    try (
        MockedStatic<AuthProvider> authProvider = mockStatic(AuthProvider.class);
        MockedStatic<Provisioning> provisioning = mockStatic(Provisioning.class)
    ) {
      provisioning.when(Provisioning::getInstance).thenReturn(mockProvisioning);
      authProvider.when(() -> AuthProvider.getAuthToken(mockAccount)).thenThrow(authError);

      ServiceException thrown = assertThrows(
          ServiceException.class,
          () -> AuthTokenCacheHelper.getValidAuthToken(mockAccount)
      );
      assertEquals("failure:Token generation failed", thrown.getMessage());
    }
  }


  @Test
  void shouldDeleteCachedTokenOnValidationFailure() throws Exception {
    AuthToken invalidToken = mock(AuthToken.class);
    Provisioning mockProvisioning = mock(Provisioning.class);
    when(invalidToken.getEncoded()).thenReturn("invalid-token");
    when(mockToken.getEncoded()).thenReturn("valid-token");

    try (MockedStatic<AuthProvider> authProvider = mockStatic(AuthProvider.class);
        MockedStatic<Provisioning> provisioning = mockStatic(Provisioning.class)) {
      provisioning.when(Provisioning::getInstance).thenReturn(mockProvisioning);

      // 1st call → returns invalid token
      // 2nd call → returns valid token
      authProvider
          .when(() -> AuthProvider.getAuthToken(mockAccount))
          .thenReturn(invalidToken) // first call
          .thenReturn(mockToken); // second call

      // validation fails for the invalid token
      authProvider
          .when(() -> AuthProvider.validateAuthToken(mockProvisioning, invalidToken, true))
          .thenThrow(ServiceException.FAILURE("invalid", null));

      // validation succeeds for the valid token
      authProvider
          .when(() -> AuthProvider.validateAuthToken(mockProvisioning, mockToken, true))
          .thenReturn(null);

      // First attempt → triggers invalidation of invalidToken
      try {
        AuthTokenCacheHelper.getValidAuthToken(mockAccount);
      } catch (ServiceException ignored) {
        // Ignored — expected failure on invalid token
      }

      // Second attempt → should regenerate and return valid token
      AuthToken validToken = AuthTokenCacheHelper.getValidAuthToken(mockAccount);
      assertEquals("valid-token", validToken.getEncoded(), "Should return regenerated valid token");
    }
  }
}
