package com.zimbra.cs.account.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Unit test for {@link LdapProvisioning}.
 *
 */
public class LdapProvisioningTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    HashMap<String,Object> attrs;
    attrs = new HashMap<>();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    attrs.put(Provisioning.A_sn, "Leesa");
    attrs.put(Provisioning.A_cn, "Natalie Leesa");
    attrs.put(Provisioning.A_initials, "James");
    attrs.put(Provisioning.A_mail, "natalie.leesa@example.com");
    prov.createAccount("natalie.leesa@example.com", "testpassword", attrs);
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  /**
   * This test is to validate functionality of
   * ({@link com.zimbra.cs.account.ldap.LdapProvisioning#validatePasswordEntropyForPersonalData(String, Account)})
   * @throws ServiceException */
  @Test
  public void shouldFindPersonalDataInPassword() throws ServiceException {

    //Account acct = Provisioning.getInstance().get(AccountBy.id, "natalie.leesa@example.com");
    Account acct = Provisioning.getInstance().getAccount("natalie.leesa@example.com");

    //fake passwords for test against created user account
    String[] testPasswords = {"natalie", "Natalie123", "Leesa34", "example01", "Leesa.Example", "eXamPle", "2022james"};

    // get possible personal data for account
    Optional<String> sName =
        Optional.of(Optional.ofNullable(acct.getAttr(Provisioning.A_sn)).orElse(""));
    Optional<String> cName =
        Optional.of(Optional.ofNullable(acct.getAttr(Provisioning.A_cn)).orElse(""));
    Optional<String> initials =
        Optional.of(Optional.ofNullable(acct.getAttr(Provisioning.A_initials)).orElse(""));
    Optional<String> email =
        Optional.of(Optional.ofNullable(acct.getAttr(Provisioning.A_mail)).orElse(""));

    // prepare dictionary
    String[] fullNameExploded = cName.get().toLowerCase().split(" ");
    String[] emailExploded = email.get().split("[@._]");
    String[] cleanEmailExploded =
        Arrays.copyOf(emailExploded, emailExploded.length - 1); // remove the top level domain(TLD)
    Arrays.stream(cleanEmailExploded)
        .forEach(
            em -> {
              System.out.println("email element" + em);
            });

    // find match in dictionary and password
    Arrays.stream(testPasswords).map(String::toLowerCase).forEach(passwordLower -> {
      String[] personalDataImploded = {
          sName.get().toLowerCase(),
          cName.get().toLowerCase(),
          cName.get().replace(" ", "").toLowerCase(),
          initials.get().toLowerCase()
      };
      Optional<String> matchedSensitivePart =
          Stream.of(personalDataImploded, cleanEmailExploded, fullNameExploded)
              .flatMap(Stream::of)
              .parallel()
              .filter(
                  part ->
                      (part.length() > 2
                          && (part.contains(passwordLower) || passwordLower.contains(part))))
              .findAny();
      Assert.assertTrue("matching against password:" + passwordLower,
          matchedSensitivePart.isPresent());
    });
  }
  
}