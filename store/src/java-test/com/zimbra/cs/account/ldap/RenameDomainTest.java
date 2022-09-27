package com.zimbra.cs.account.ldap;

import static org.mockito.Mockito.mock;

import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.RenameDomain.RenameDomainLdapHelper;
import com.zimbra.cs.ldap.ILdapContext;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.HashMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RenameDomainTest {

  private static Provisioning provisioning;

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    provisioning = Provisioning.getInstance();
  }

  @Before
  public void clearData() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void shouldRenameVirtualHostnamesWhenRenamingDomain() throws Exception {
    ILdapContext zlc = null;
    final LdapProvisioning ldapProv = new LdapProvisioning();
    final String domainName = "demo.zextras.io";
    final String newDomainName = "new.demo.zextras.io";
    final String newPubServiceHostname = "this.domain.iz.fake";
    final Domain domain = ldapProv.createDomain(domainName, new HashMap<>());
    final RenameDomainLdapHelper ldapHelper = mock(RenameDomainLdapHelper.class);
    new RenameDomain(ldapProv, ldapHelper, domain, newDomainName).execute();
    final Domain gotOldDomain = ldapProv.getDomainByName(domainName);
    final Domain newOldDomain = ldapProv.getDomainByName(newDomainName);
  }
}
