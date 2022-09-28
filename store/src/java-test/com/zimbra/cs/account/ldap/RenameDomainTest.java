package com.zimbra.cs.account.ldap;

import static com.zimbra.common.localconfig.LocalConfig.LOCALCONFIG_KEY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.RenameDomain.RenameDomainLdapHelper;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RenameDomainTest {

  private static Provisioning provisioning;

  @BeforeClass
  public static void init() throws Exception {
    System.setProperty(
        LOCALCONFIG_KEY,
        Path.of(RenameDomainTest.class.getResource("localconfig-ldap-test.xml").toURI())
            .toString());
    MailboxTestUtil.initServer();
    provisioning = Provisioning.getInstance();
  }

  @Before
  public void clearData() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void shouldRenameVirtualHostnamesAndPublicHostnameWhenRenamingDomain() throws Exception {
    final LdapProvisioning ldapProv = new LdapProvisioning();
    final String domainName = "demo.zextras.io";
    final String[] virtualHostnames =
        new String[] {"virtual1." + domainName, "virtual2." + domainName};
    final String publicHostname = "web." + domainName;
    final String newDomainName = "new.demo.zextras.io";
    // cleanup just in case
    Try.run(() -> ldapProv.deleteDomain(ldapProv.getDomainByName(newDomainName).getId()));
    Try.run(() -> ldapProv.deleteDomain(ldapProv.getDomainByName(newDomainName).getId()));
    final Domain domain =
        ldapProv.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicHostname);
                put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostnames);
              }
            });
    final RenameDomainLdapHelper ldapHelper = mock(RenameDomainLdapHelper.class);
    new RenameDomain(ldapProv, ldapHelper, domain, newDomainName).execute();
    assertNull(ldapProv.getDomainByName(domainName));
    final Domain gotNewDomain = ldapProv.getDomainByName(newDomainName);
    assertEquals(newDomainName, gotNewDomain.getDomainName());
    assertEquals("web." + newDomainName, gotNewDomain.getPublicServiceHostname());
    final String[] expectedVirtualHostnames =
        new String[] {"virtual1." + newDomainName, "virtual2." + newDomainName};
    assertTrue(
        Arrays.stream(expectedVirtualHostnames)
            .collect(Collectors.toList())
            .containsAll(
                Arrays.stream(gotNewDomain.getVirtualHostname()).collect(Collectors.toList())));
  }
}
