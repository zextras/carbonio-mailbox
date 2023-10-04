// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.util;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.zimbra.common.localconfig.LC;

public class InMemoryLdapServerTestUtil {

  public static InMemoryDirectoryServer createInMemoryDirectoryServer(int ldapPort)
      throws Exception {
    InMemoryDirectoryServerConfig ldapServerConfig =
        new InMemoryDirectoryServerConfig(
            "dc=com",
            "cn=config",
            "cn=defaultExternal,cn=cos,cn=zimbra",
            "cn=default,cn=cos,cn=zimbra",
            "cn=config,cn=zimbra",
            "cn=zimbra");
    ldapServerConfig.addAdditionalBindCredentials("cn=config", LC.ldap_root_password.value());
    ldapServerConfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", ldapPort));
    ldapServerConfig.setSchema(null);
    ldapServerConfig.setGenerateOperationalAttributes(true);

    InMemoryDirectoryServer inMemoryLdapServer = new InMemoryDirectoryServer(ldapServerConfig);
    inMemoryLdapServer.importFromLDIF(true, "./build/ldap/config/cn=config.ldif");
    inMemoryLdapServer.importFromLDIF(false, "./build/ldap/zimbra_globalconfig.ldif");
    inMemoryLdapServer.importFromLDIF(false, "./build/ldap/zimbra_defaultcos.ldif");
    inMemoryLdapServer.importFromLDIF(false, "./build/ldap/zimbra_defaultexternalcos.ldif");
    inMemoryLdapServer.importFromLDIF(false, "./build/ldap/carbonio.ldif");
    inMemoryLdapServer.startListening();
    // update password for admin
    inMemoryLdapServer
        .getConnection()
        .modify(
            "uid=zimbra,cn=admins,cn=zimbra",
            new Modification(
                ModificationType.REPLACE, "userPassword", LC.zimbra_ldap_password.value()));
    return inMemoryLdapServer;
  }
}
