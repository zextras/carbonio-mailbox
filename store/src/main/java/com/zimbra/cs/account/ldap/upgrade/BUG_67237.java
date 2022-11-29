// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.account.Key.CosBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Entry.EntryType;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;

public class BUG_67237 extends UpgradeOp {
    private static final String ATTR_NAME = Provisioning.A_zimbraPrefShowSelectionCheckbox;
    private static final String OLD_VALUE = ProvisioningConstants.TRUE;
    private static final String NEW_VALUE = ProvisioningConstants.FALSE;

    @Override
    void doUpgrade() throws ServiceException {
        ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
        try {
            doDefaultCos(zlc);
        } finally {
            LdapClient.closeContext(zlc);
        }
    }

    @Override
    Description getDescription() {
        return new Description(this,
                new String[] { ATTR_NAME },
                new EntryType[] { EntryType.COS },
                OLD_VALUE,
                NEW_VALUE,
                "Upgrades only the default cos.");
    }

    private void doEntry(ZLdapContext zlc, Entry entry) throws ServiceException {
        String entryName = entry.getLabel();

        printer.println();
        printer.println("------------------------------");
        printer.println("Checking " + ATTR_NAME + " on " + entryName);

        String curValue = entry.getAttr(ATTR_NAME, false);
        if (OLD_VALUE.equals(curValue)) {
            printer.println(
                    "    Changing " + ATTR_NAME + " on " + entryName + " from " + curValue + " to " + NEW_VALUE);

            Map<String, Object> attr = new HashMap<String, Object>();
            attr.put(ATTR_NAME, NEW_VALUE);
            try {
                modifyAttrs(zlc, entry, attr);
            } catch (ServiceException e) {
                // log the exception and continue
                printer.println("Caught ServiceException while modifying " + entryName + " attribute " + attr);
                printer.printStackTrace(e);
            }
        } else {
            printer.println(
                    "    Current value of " + ATTR_NAME + " on " + entryName + " is " + curValue + " - not changed");
        }
    }

    private void doDefaultCos(ZLdapContext zlc) throws ServiceException {
        Cos cos = prov.get(CosBy.name, Provisioning.DEFAULT_COS_NAME);
        if (cos != null) {
            doEntry(zlc, cos);
        }
    }
}
