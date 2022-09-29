package com.zimbra.cs.account.ldap;

public class RenameDomainTest {

  //  // RenameDomain is specific for ldap
  //  private static LdapProvisioning ldapProvisioning;
  //  private static LdapHelper ldapHelper;
  //  private GenericContainer container =
  //      new GenericContainer<>("zextras.jfrog.io/carbonio/ce-ldap-u20:latest")
  //          .withCreateContainerCmdModifier(it -> it.withHostName("ldap.mail.local"))
  //          .withExposedPorts(389)
  //          .withImagePullPolicy(PullPolicy.defaultPolicy());
  //
  //  @Before
  //  public void init() throws Exception {
  //    container.start();
  //    // default values for container
  //    LC.ldap_port.setDefault(container.getMappedPort(389));
  //    LC.zimbra_ldap_password.setDefault("password");
  //    MailboxTestUtil.initServer();
  //    ldapProvisioning = new LdapProvisioning();
  //    ldapHelper = ldapProvisioning.getHelper();
  //  }
  //
  //  @After
  //  public void clearData() throws Exception {
  //    MailboxTestUtil.clearData();
  //    container.stop();
  //  }
  //
  //  @Test
  //  public void shouldRenameVirtualHostnamesAndPublicHostnameWhenRenamingDomain() throws Exception
  // {
  //    final String domainName = "carbonio.io";
  //    final String[] virtualHostnames =
  //        new String[] {"virtual1." + domainName, "virtual2." + domainName};
  //    final String publicHostname = "web." + domainName;
  //    final HashMap<String, Object> domainAttrs =
  //        new HashMap<>() {
  //          {
  //            put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicHostname);
  //            put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostnames);
  //          }
  //        };
  //    final Domain domain = ldapProvisioning.createDomain(domainName, domainAttrs);
  //    final String newDomainName = "new.demo.zextras.io";
  //
  //    final RenameDomainLdapHelper ldapHelper = mock(RenameDomainLdapHelper.class);
  //    new RenameDomain(ldapProvisioning, ldapHelper, domain, newDomainName).execute();
  //    assertNull(ldapProvisioning.getDomainByName(domainName));
  //    final Domain gotNewDomain = ldapProvisioning.getDomainByName(newDomainName);
  //    assertEquals(newDomainName, gotNewDomain.getDomainName());
  //    assertEquals("web." + newDomainName, gotNewDomain.getPublicServiceHostname());
  //    final String[] expectedVirtualHostnames =
  //        new String[] {"virtual1." + newDomainName, "virtual2." + newDomainName};
  //    assertTrue(
  //        Arrays.stream(expectedVirtualHostnames)
  //            .collect(Collectors.toList())
  //            .containsAll(
  //                Arrays.stream(gotNewDomain.getVirtualHostname()).collect(Collectors.toList())));
  //  }
  //
  //  @Test
  //  public void shouldNotLoseAliasesWhenDomainRenamed() throws Exception {
  //    final String domainName = "demo.zextras.io";
  //    final String newDomainName = "new.demo.zextras.io";
  //    final Domain domain = ldapProvisioning.createDomain(domainName, new HashMap<>());
  //    final String domainAliasTargetId =
  //        ldapProvisioning
  //            .createDomain(
  //                "alias.whatever",
  //                new HashMap<>() {
  //                  {
  //                    put(Provisioning.A_zimbraDomainType, Provisioning.DomainType.alias.name());
  //                    put(Provisioning.A_zimbraDomainAliasTargetId, domain.getId());
  //                  }
  //                })
  //            .getDomainAliasTargetId();
  //    final RenameDomainLdapHelper ldapHelper = mock(RenameDomainLdapHelper.class);
  //    new RenameDomain(ldapProvisioning, ldapHelper, domain, newDomainName).execute();
  //    // get new domain and check alias still points to it
  //    final Domain gotNewDomain = ldapProvisioning.getDomainByName(newDomainName);
  //    assertEquals(domainAliasTargetId, domain.getId());
  //    assertEquals(domain.getId(), gotNewDomain.getId());
  //  }
  //
  //  @Test
  //  public void shouldRenameVirtualHostnamesAndPublicHostnameWhenRenamingAliasDomain()
  //      throws Exception {
  //    final LdapProvisioning ldapProv = new LdapProvisioning();
  //    final String domainName = "demo.zextras.io";
  //    final String aliasDomainName = "this.is.my.alias";
  //    final String[] aliasVirtualHostnames =
  //        new String[] {"virtual1." + aliasDomainName, "virtual2." + aliasDomainName};
  //    final String aliasPublicHostname = "web." + aliasDomainName;
  //    final String newAliasDomainName = "new.demo.zextras.io";
  //    // cleanup just in case
  //
  //    final Domain domain = ldapProv.createDomain(domainName, new HashMap<>());
  //    final Domain aliasDomain =
  //        ldapProv.createDomain(
  //            domainName,
  //            new HashMap<>() {
  //              {
  //                put(ZAttrProvisioning.A_zimbraPublicServiceHostname, aliasPublicHostname);
  //                put(ZAttrProvisioning.A_zimbraVirtualHostname, aliasVirtualHostnames);
  //                put(Provisioning.A_zimbraDomainType, Provisioning.DomainType.alias.name());
  //                put(Provisioning.A_zimbraDomainAliasTargetId, domain.getId());
  //              }
  //            });
  //
  //    final RenameDomainLdapHelper ldapHelper = mock(RenameDomainLdapHelper.class);
  //    new RenameDomain(ldapProv, ldapHelper, aliasDomain, newAliasDomainName).execute();
  //    assertNull(ldapProv.getDomainByName(aliasDomainName));
  //    final Domain gotNewAliasDomain = ldapProv.getDomainByName(newAliasDomainName);
  //    assertEquals(newAliasDomainName, gotNewAliasDomain.getDomainName());
  //    assertEquals("web." + newAliasDomainName, gotNewAliasDomain.getPublicServiceHostname());
  //    final String[] expectedVirtualHostnames =
  //        new String[] {"virtual1." + newAliasDomainName, "virtual2." + newAliasDomainName};
  //    assertTrue(
  //        Arrays.stream(expectedVirtualHostnames)
  //            .collect(Collectors.toList())
  //            .containsAll(
  //                Arrays.stream(gotNewAliasDomain.getVirtualHostname())
  //                    .collect(Collectors.toList())));
  //  }
}
