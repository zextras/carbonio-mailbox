// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Entry.EntryType;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BUG_88766 extends UpgradeOp {

  private static final String ATTR_NAME = Provisioning.A_zimbraPrefHtmlEditorDefaultFontFamily;
  private static final String FROM_VALUE = "times new roman, new york, times, serif";
  private static final String TO_VALUE = "arial, helvetica, sans-serif";

  @Override
  void doUpgrade() throws ServiceException {
    ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
    try {
      doCos(zlc);
    } finally {
      LdapClient.closeContext(zlc);
    }
  }

  @Override
  Description getDescription() {
    return new Description(
        this,
        new String[] {ATTR_NAME},
        new EntryType[] {EntryType.COS},
        FROM_VALUE,
        TO_VALUE,
        "Update html editor fonts to 8.5 default for all COSes where it is set to the previous"
            + " default");
  }

  private void doCos(ZLdapContext zlc) throws ServiceException {
    List<Cos> classes = prov.getAllCos();
    if (classes != null) {
      for (Cos cos : classes) {
        String fonts = cos.getAttr(ATTR_NAME, "");
        if (FROM_VALUE.equalsIgnoreCase(fonts)) {
          Map<String, Object> attrs = new HashMap<String, Object>();
          attrs.put(ATTR_NAME, TO_VALUE);
          modifyAttrs(cos, attrs);
        }
      }
    }
  }
}
