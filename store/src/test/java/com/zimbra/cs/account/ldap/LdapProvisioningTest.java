package com.zimbra.cs.account.ldap;

import static com.zimbra.cs.account.Provisioning.AUTH_MODE_KEY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning.FeatureResetPasswordStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AccountServiceException.AuthFailedServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.AuthMode;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.account.auth.AuthContext.Protocol;
import com.zimbra.cs.account.auth.AuthMechanism.AuthMech;
import com.zimbra.cs.account.auth.ZimbraCustomAuth;
import com.zimbra.cs.account.auth.ZimbraCustomAuthTest.TestCustomAuth;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link LdapProvisioning}.
 */
public class LdapProvisioningTest {

  private static Provisioning provisioning;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.setUp();
    provisioning = Provisioning.getInstance();

    provisioning.createDomain("example.com", new HashMap<>());

    HashMap<String, Object> exampleAccountAttrs;
    exampleAccountAttrs = new HashMap<>();
    exampleAccountAttrs.put(Provisioning.A_sn, "Leesa");
    exampleAccountAttrs.put(Provisioning.A_cn, "Natalie Leesa");
    exampleAccountAttrs.put(Provisioning.A_initials, "James");
    provisioning.createAccount("natalie.leesa@example.com", "testpassword", exampleAccountAttrs);

    provisioning.createDomain("lamborghini.io", new HashMap<>());
    HashMap<String, Object> exampleAccountAttrs2;
    exampleAccountAttrs2 = new HashMap<>();
    exampleAccountAttrs2.put(Provisioning.A_sn, "Lamborghini");
    exampleAccountAttrs2.put(Provisioning.A_cn, "Milano");
    exampleAccountAttrs2.put(Provisioning.A_initials, "Cars");
    provisioning.createAccount("milano@lamborghini.io", "testpassword", exampleAccountAttrs2);

    provisioning.createDomain("lamborghini-europe.com", new HashMap<>());
    HashMap<String, Object> exampleAccountAttrs3;
    exampleAccountAttrs3 = new HashMap<>();
    exampleAccountAttrs3.put(Provisioning.A_sn, "Lamborghini");
    exampleAccountAttrs3.put(Provisioning.A_cn, "Milano");
    exampleAccountAttrs3.put(Provisioning.A_initials, "Cars");
    provisioning.createAccount("milano@lamborghini-europe.com", "testpassword", exampleAccountAttrs3);
  }

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  /**
   * This test is to validate functionality of ({@link com.zimbra.cs.account.ldap.LdapProvisioning#validatePasswordEntropyForPersonalData(String,
   * Account)}) for personal data
   */
  @Test
  void shouldFindPersonalDataInPassword() {
    assertThrows(AccountServiceException.class, () -> {

      Account acct = provisioning.getAccount("natalie.leesa@example.com");

      // fake passwords for test against created user account
      String[] testPasswords = {
          "natalie", "Natalie123", "Leesa34", "example01", "Leesa.Example", "eXamPle", "2022james"
      };
      for (String testPassword : testPasswords) {
        String passwordLower = testPassword.toLowerCase();
        LdapProvisioning.validatePasswordEntropyForPersonalData(passwordLower, acct);
      }
    });
  }

  /**
   * This test is to validate functionality of ({@link com.zimbra.cs.account.ldap.LdapProvisioning#validatePasswordEntropyForPersonalData(String,
   * Account)}) for specific case in which user might be <b>using a dotless domain</b>
   *
   * @throws ServiceException
   */
  @Test
  void shouldFindPersonalDataInPasswordForDotLessDomainsEmails() throws ServiceException {
    assertThrows(AccountServiceException.class, () -> {

      Account acct = provisioning.getAccount("milano@lamborghini.io");

      // fake passwords for test against created user account
      String[] testPasswords = {
          "milano",
          "Lamborghini123",
          "Milano34",
          "lamborghini01",
          "Milano.Lamborghini",
          "lAmbOrgHini",
          "2022cars"
      };

      for (String testPassword : testPasswords) {
        String passwordLower = testPassword.toLowerCase();
        LdapProvisioning.validatePasswordEntropyForPersonalData(passwordLower, acct);
      }
    });
  }

  /**
   * This test is to validate functionality of ({@link com.zimbra.cs.account.ldap.LdapProvisioning#validatePasswordEntropyForPersonalData(String,
   * Account)}) for specific case in which user's <b>domain name contains special chars</b>
   */
  @Test
  void shouldFindPersonalDataInPasswordForDomainsContainingSpecialChars() {
    assertThrows(AccountServiceException.class, () -> {

      Account acct = provisioning.getAccount("milano@lamborghini-europe.com");

      // fake passwords for test against created user account
      String[] testPasswords = {"Adjbiuhkl2022europe", "Lamborghini123"};

      for (String testPassword : testPasswords) {
        String passwordLower = testPassword.toLowerCase();
        LdapProvisioning.validatePasswordEntropyForPersonalData(passwordLower, acct);
      }
    });
  }

  /**
   * Tests CO-284 When zimbraAuthfallbackToLocal is FALSE, auth should not fallback to local even for admin Tested
   * against external ldap authentication
   */
  @Test
  void shouldNotFallbackAdminAuthWhenFallbackFalse() throws Exception {
    final String domain = "demo.com";
    Map<String, Object> domainAttrs = new HashMap<>();
    domainAttrs.put(Provisioning.A_zimbraAuthFallbackToLocal, "FALSE");
    domainAttrs.put(Provisioning.A_zimbraAuthMech, AuthMech.ldap.toString());
    provisioning.createDomain(domain, domainAttrs);

    HashMap<String, Object> exampleAccountAttrs3 = new HashMap<>();
    exampleAccountAttrs3.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
    final String email = "testAdmin@" + domain;
    final String password = "testPassword";
    final Account testAdminAccount = provisioning.createAccount(email, password, exampleAccountAttrs3);

    assertThrows(ServiceException.class, () -> {
      final HashMap<String, Object> authContext = new HashMap<>();
      authContext.put(AuthContext.AC_AS_ADMIN, true);
      provisioning.authAccount(testAdminAccount, password, Protocol.soap, authContext );
    });
  }

  /**
   * Tests CO-284 When zimbraAuthfallbackToLocal is FALSE, auth should fallback to local even for admin
   */
  @Test
  void shouldFallbackAdminAuthWhenFallbackTrue() throws Exception {
    final String domain = UUID.randomUUID() + ".com";
    Map<String, Object> domainAttrs = new HashMap<>();
    domainAttrs.put(Provisioning.A_zimbraAuthMech, AuthMech.ldap.toString());
    provisioning.createDomain(domain, domainAttrs);

    HashMap<String, Object> exampleAccountAttrs3 = new HashMap<>();
    exampleAccountAttrs3.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    exampleAccountAttrs3.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
    final String password = "testPassword";
    final Account testAdminAccount = provisioning.createAccount("testAdmin@" + domain, password, exampleAccountAttrs3);

    assertDoesNotThrow(() -> provisioning.authAccount(testAdminAccount, password, Protocol.soap));
  }

  @Test
  void aliasDomainShouldHaveZimbraMailCatchAllForwardingAddressSetByDefault() throws ServiceException {
    Provisioning prov = provisioning;

    // create target domain
    final String domainName = "co477.com";
    final String aliasDomainName = "aka.co477.com";
    final Domain targetDomain = prov.createDomain(domainName, new HashMap<String, Object>());

    // create alias domain
    final HashMap<String, Object> attrs = new HashMap<>();
    attrs.put(Provisioning.A_zimbraDomainType, Provisioning.DomainType.alias.name());
    attrs.put(Provisioning.A_zimbraDomainAliasTargetId, targetDomain.getId());
    final Domain aliasDomain = prov.createDomain(aliasDomainName, attrs);

    assertEquals(aliasDomain.getAttr(Provisioning.A_zimbraMailCatchAllForwardingAddress),
        "@" + domainName);
  }

  @Test
  void should_throw_exception_when_recovery_code_based_auth_and_carbonio_auth_mechanism_is_registered() throws ServiceException {
    ZimbraCustomAuth.register(AuthMech.carbonioAdvanced.name(), new TestCustomAuth());

    var domain = UUID.randomUUID() + ".com";
    var domainAttrs = new HashMap<String, Object>();
    domainAttrs.put(Provisioning.A_zimbraAuthMech, AuthMech.carbonioAdvanced.name());
    provisioning.createDomain(domain, domainAttrs);

    var password = "testPassword";
    var account = provisioning.createAccount(UUID.randomUUID() + "@" + domain, password, new HashMap<>());
    account.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

    var authFailedServiceException = assertThrows(AuthFailedServiceException.class,
        () -> provisioning.authAccount(account, password, Protocol.http_basic,
            createAuthContext(AuthMode.RECOVERY_CODE)));

    assertEquals(AccountServiceException.CANNOT_PERFORM_AUTH_WHEN_ADVANCED_AUTH_IS_ENABLED_CODE,
        authFailedServiceException.getCode());
  }

  @Test
  void should_throw_auth_failed_exception_when_recovery_code_based_auth_and_carbonio_auth_mechanism_is_registered() throws ServiceException {
    ZimbraCustomAuth.register(AuthMech.carbonioAdvanced.name(), new TestCustomAuth());

    var domain = UUID.randomUUID() + ".com";
    var domainAttrs = new HashMap<String, Object>();
    domainAttrs.put(Provisioning.A_zimbraAuthMech, AuthMech.carbonioAdvanced.name());
    provisioning.createDomain(domain, domainAttrs);

    var wrongRecoveryCode = UUID.randomUUID().toString();
    var account = provisioning.createAccount(UUID.randomUUID() + "@" + domain, wrongRecoveryCode, new HashMap<>());
    account.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

    var authContext = createAuthContext(AuthMode.RECOVERY_CODE);
    authContext.put("recoveryTokenBasedAuth", true); // mimic carbonio advanced auth recovery token based authentication request

    var authFailedServiceException = assertThrows(AuthFailedServiceException.class,
        () -> provisioning.authAccount(account, wrongRecoveryCode, Protocol.http_basic, authContext
            ));
    assertEquals(AccountServiceException.AUTH_FAILED, authFailedServiceException.getCode());
  }

  @SuppressWarnings("SameParameterValue")
  private Map<String, Object> createAuthContext(AuthMode authMode) {
    Map<String, Object> authContext = new HashMap<>();
    authContext.put(AUTH_MODE_KEY, authMode);
    return authContext;
  }

}
