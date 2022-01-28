package com.zimbra.cs.account.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Unit test for {@link LdapProvisioning}. */
public class LdapProvisioningTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    HashMap<String, Object> exampleAccountAttrs;
    exampleAccountAttrs = new HashMap<>();
    exampleAccountAttrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    exampleAccountAttrs.put(Provisioning.A_sn, "Leesa");
    exampleAccountAttrs.put(Provisioning.A_cn, "Natalie Leesa");
    exampleAccountAttrs.put(Provisioning.A_initials, "James");
    exampleAccountAttrs.put(Provisioning.A_mail, "natalie.leesa@example.com");
    prov.createAccount("natalie.leesa@example.com", "testpassword", exampleAccountAttrs);

    HashMap<String, Object> exampleAccountAttrs2;
    exampleAccountAttrs2 = new HashMap<>();
    exampleAccountAttrs2.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    exampleAccountAttrs2.put(Provisioning.A_sn, "Lamborghini");
    exampleAccountAttrs2.put(Provisioning.A_cn, "Milano");
    exampleAccountAttrs2.put(Provisioning.A_initials, "Cars");
    exampleAccountAttrs2.put(Provisioning.A_mail, "milano@lamborghini");
    prov.createAccount("milano@lamborghini", "testpassword", exampleAccountAttrs2);

    HashMap<String, Object> exampleAccountAttrs3;
    exampleAccountAttrs3 = new HashMap<>();
    exampleAccountAttrs3.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    exampleAccountAttrs3.put(Provisioning.A_sn, "Lamborghini");
    exampleAccountAttrs3.put(Provisioning.A_cn, "Milano");
    exampleAccountAttrs3.put(Provisioning.A_initials, "Cars");
    exampleAccountAttrs3.put(Provisioning.A_mail, "milano@lamborghini-europe.com");
    prov.createAccount("milano@lamborghini-europe.com", "testpassword", exampleAccountAttrs3);
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  /**
   * This test is to validate functionality of ({@link
   * com.zimbra.cs.account.ldap.LdapProvisioning#validatePasswordEntropyForPersonalData(String,
   * Account)}) for personal data
   *
   * @throws ServiceException
   */
  @Test(expected = AccountServiceException.class)
  public void shouldFindPersonalDataInPassword() throws ServiceException {

    Account acct = Provisioning.getInstance().getAccount("natalie.leesa@example.com");

    // fake passwords for test against created user account
    String[] testPasswords = {
      "natalie", "Natalie123", "Leesa34", "example01", "Leesa.Example", "eXamPle", "2022james"
    };
    for (String testPassword : testPasswords) {
      String passwordLower = testPassword.toLowerCase();
      LdapProvisioning.validatePasswordEntropyForPersonalData(passwordLower, acct);
    }
  }

  /**
   * This test is to validate functionality of ({@link
   * com.zimbra.cs.account.ldap.LdapProvisioning#validatePasswordEntropyForPersonalData(String,
   * Account)}) for specific case in which user might be <b>using a dotless domain</b>
   *
   * @throws ServiceException
   */
  @Test(expected = AccountServiceException.class)
  public void shouldFindPersonalDataInPasswordForDotLessDomainsEmails() throws ServiceException {

    Account acct = Provisioning.getInstance().getAccount("milano@lamborghini");

    // fake passwords for test against created user account
    String[] testPasswords = {
      "milano", "Lamborghini123", "Milano34", "lamborghini01", "Milano.Lamborghini", "lAmbOrgHini", "2022cars"
    };

    for (String testPassword : testPasswords) {
      String passwordLower = testPassword.toLowerCase();
      LdapProvisioning.validatePasswordEntropyForPersonalData(passwordLower, acct);
    }
  }

  /**
   * This test is to validate functionality of ({@link
   * com.zimbra.cs.account.ldap.LdapProvisioning#validatePasswordEntropyForPersonalData(String,
   * Account)}) for specific case in which user's <b>domain name contains special chars</b>
   *
   * @throws ServiceException
   */
  @Test(expected = AccountServiceException.class)
  public void shouldFindPersonalDataInPasswordForDomainsContainingSpecialChars() throws ServiceException {

    Account acct = Provisioning.getInstance().getAccount("milano@lamborghini-europe.com");

    // fake passwords for test against created user account
    String[] testPasswords = {
        "Adjbiuhkl2022europe", "Lamborghini123"
    };

    for (String testPassword : testPasswords) {
      String passwordLower = testPassword.toLowerCase();
      LdapProvisioning.validatePasswordEntropyForPersonalData(passwordLower, acct);
    }
  }
}
