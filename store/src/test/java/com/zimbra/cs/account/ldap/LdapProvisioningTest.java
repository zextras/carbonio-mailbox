package com.zimbra.cs.account.ldap;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.AuthContext.Protocol;
import com.zimbra.cs.account.auth.AuthMechanism.AuthMech;
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

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.setUp();
        Provisioning provisioning = Provisioning.getInstance();

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
     *
     * @throws ServiceException
     */
    @Test
    void shouldFindPersonalDataInPassword() throws ServiceException {
        assertThrows(AccountServiceException.class, () -> {

            Account acct = Provisioning.getInstance().getAccount("natalie.leesa@example.com");

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

            Account acct = Provisioning.getInstance().getAccount("milano@lamborghini.io");

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
     *
     * @throws ServiceException
     */
    @Test
    void shouldFindPersonalDataInPasswordForDomainsContainingSpecialChars()
            throws ServiceException {
        assertThrows(AccountServiceException.class, () -> {

            Account acct = Provisioning.getInstance().getAccount("milano@lamborghini-europe.com");

            // fake passwords for test against created user account
            String[] testPasswords = {"Adjbiuhkl2022europe", "Lamborghini123"};

            for (String testPassword : testPasswords) {
                String passwordLower = testPassword.toLowerCase();
                LdapProvisioning.validatePasswordEntropyForPersonalData(passwordLower, acct);
            }
        });
    }

    /**
     * Tests CO-284 When zimbraAuthfallbackToLocal is FALSE, auth should not fallback to local even
     * for admin Tested against external ldap authentication
     */
    @Test
    void shouldNotFallbackAdminAuthWhenFallbackFalse() throws Exception {
        assertThrows(ServiceException.class, () -> {
            Provisioning prov = Provisioning.getInstance();
            // create domain
            final String domain = "demo.com";
            Map<String, Object> domainAttrs = new HashMap<>();
            domainAttrs.put(Provisioning.A_zimbraAuthMech, AuthMech.ldap.toString());
            prov.createDomain(domain, domainAttrs);
            // create account
            HashMap<String, Object> exampleAccountAttrs3;
            exampleAccountAttrs3 = new HashMap<>();
            exampleAccountAttrs3.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
            exampleAccountAttrs3.put(Provisioning.A_sn, "Demo");
            exampleAccountAttrs3.put(Provisioning.A_cn, "Test");
            exampleAccountAttrs3.put(Provisioning.A_initials, "Admin");
            exampleAccountAttrs3.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
            final String email = "testAdmin@" + domain;
            final String password = "testPassword";
            exampleAccountAttrs3.put(Provisioning.A_mail, email);
            final Account testAdminAccount = prov.createAccount(email, password, exampleAccountAttrs3);
            prov.authAccount(testAdminAccount, password, Protocol.soap);
        });
    }

    /**
     * Tests CO-284 When zimbraAuthfallbackToLocal is FALSE, auth should fallback to local even for
     * admin
     */
    @Test
    void shouldFallbackAdminAuthWhenFallbackTrue() throws Exception {
        Provisioning prov = Provisioning.getInstance();
        // create domain
        final String domain = "demo.com";
        Map<String, Object> domainAttrs = new HashMap<>();
        domainAttrs.put(Provisioning.A_zimbraAuthMech, AuthMech.ldap.toString());
        // create account
        HashMap<String, Object> exampleAccountAttrs3;
        exampleAccountAttrs3 = new HashMap<>();
        exampleAccountAttrs3.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        exampleAccountAttrs3.put(Provisioning.A_sn, "Demo");
        exampleAccountAttrs3.put(Provisioning.A_cn, "Test");
        exampleAccountAttrs3.put(Provisioning.A_initials, "Admin");
        exampleAccountAttrs3.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
        final String email = "testAdmin@" + domain;
        final String password = "testPassword";
        final Account testAdminAccount = prov.createAccount(email, password, exampleAccountAttrs3);
        prov.authAccount(testAdminAccount, password, Protocol.soap);
    }

    @Test
    void aliasDomainShouldHaveZimbraMailCatchAllForwardingAddressSetByDefault() throws ServiceException {
        Provisioning prov = Provisioning.getInstance();

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
}
