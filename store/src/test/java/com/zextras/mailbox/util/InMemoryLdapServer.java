// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.util;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldif.LDIFReader;
import com.zimbra.common.localconfig.LC;

/** Util class to create an {@link InMemoryDirectoryServer} */
public class InMemoryLdapServer {

  private final InMemoryDirectoryServer inMemoryDirectoryServer;
  private final String adminPassword;

  public void clear() {
    inMemoryDirectoryServer.clear();
  }

  public void shutDown(boolean shutdown) {
    inMemoryDirectoryServer.shutDown(shutdown);
  }

  public static class Builder {
    private int ldapPort = 1389;
    private String ldapRootPassword = LC.ldap_root_password.value();
    private String adminPassword = LC.zimbra_ldap_password.value();

    public Builder withLdapPort(int ldapPort) {
      this.ldapPort = ldapPort;
      return this;
    }

    public Builder withLdapRootPassword(String ldapRootPassword) {
      this.ldapRootPassword = ldapRootPassword;
      return this;
    }

    public Builder withAdminPassword(String adminPassword) {
      this.adminPassword = adminPassword;
      return this;
    }

    public InMemoryLdapServer build() throws Exception {
      InMemoryDirectoryServerConfig ldapServerConfig =
          new InMemoryDirectoryServerConfig(
              "dc=com",
              "cn=config",
              "cn=defaultExternal,cn=cos,cn=zimbra",
              "cn=default,cn=cos,cn=zimbra",
              "cn=config,cn=zimbra",
              "cn=zimbra");
      ldapServerConfig.addAdditionalBindCredentials("cn=config", ldapRootPassword);
      ldapServerConfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", ldapPort));
      ldapServerConfig.setSchema(null);
      ldapServerConfig.setGenerateOperationalAttributes(true);

      InMemoryDirectoryServer inMemoryDirectoryServer = new InMemoryDirectoryServer(ldapServerConfig);
      return new InMemoryLdapServer(inMemoryDirectoryServer, adminPassword);
    }
  }

  private LDIFReader loadLdifResource(String resource) {
    return new LDIFReader(this.getClass().getResourceAsStream(resource));
  }

  public void initializeBasicData() throws LDAPException {
    inMemoryDirectoryServer.importFromLDIF(true, this.loadLdifResource("/config/cn=config.ldif"));
    inMemoryDirectoryServer.importFromLDIF(false, this.loadLdifResource("/zimbra_globalconfig.ldif"));
    inMemoryDirectoryServer.importFromLDIF(false, this.loadLdifResource("/zimbra_defaultcos.ldif"));
    inMemoryDirectoryServer.importFromLDIF(false, this.loadLdifResource("/zimbra_defaultexternalcos.ldif"));
    inMemoryDirectoryServer.importFromLDIF(false, this.loadLdifResource("/carbonio.ldif"));
    inMemoryDirectoryServer
        .getConnection()
        .modify(
            "uid=zimbra,cn=admins,cn=zimbra",
            new Modification(
                ModificationType.REPLACE, "userPassword", adminPassword));
  }

  private InMemoryLdapServer(
      InMemoryDirectoryServer inMemoryDirectoryServer, String adminPassword) {
    this.inMemoryDirectoryServer = inMemoryDirectoryServer;
    this.adminPassword = adminPassword;
  }

  public void start() throws LDAPException {
    inMemoryDirectoryServer.startListening();
  }
}
