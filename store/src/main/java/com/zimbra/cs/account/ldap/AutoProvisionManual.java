// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

import com.zimbra.common.account.ZAttrProvisioning.AutoProvMode;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.ZAttributes;
import com.zimbra.soap.type.AutoProvPrincipalBy;
import java.util.Set;

public class AutoProvisionManual extends AutoProvision {

  private AutoProvPrincipalBy by;
  private String principal;
  private String password;

  protected AutoProvisionManual(
      LdapProv prov, Domain domain, AutoProvPrincipalBy by, String principal, String password) {
    super(prov, domain);
    this.by = by;
    this.principal = principal;
    this.password = password;
  }

  @Override
  Account handle() throws ServiceException {
    if (!autoProvisionEnabled()) {
      throw ServiceException.FAILURE(
          "MANUAL auto provision is not enabled on domain " + domain.getName(), null);
    }

    return createAccount();
  }

  private boolean autoProvisionEnabled() {
    Set<String> modesEnabled = domain.getMultiAttrSet(Provisioning.A_zimbraAutoProvMode);
    return modesEnabled.contains(AutoProvMode.MANUAL.name());
  }

  private Account createAccount() throws ServiceException {
    String acctZimbraName;
    ExternalEntry externalEntry;
    if (by == AutoProvPrincipalBy.dn) {
      ZAttributes externalAttrs = getExternalAttrsByDn(principal);
      externalEntry = new ExternalEntry(principal, externalAttrs);
      acctZimbraName = mapName(externalAttrs, null);
    } else if (by == AutoProvPrincipalBy.name) {
      externalEntry = getExternalAttrsByName(principal);
      acctZimbraName = mapName(externalEntry.getAttrs(), principal);
    } else {
      throw ServiceException.FAILURE("unknown AutoProvPrincipalBy", null);
    }

    ZimbraLog.autoprov.info("auto creating account in MANUAL mode: " + acctZimbraName);
    return createAccount(acctZimbraName, externalEntry, password, AutoProvMode.MANUAL);
  }
}
