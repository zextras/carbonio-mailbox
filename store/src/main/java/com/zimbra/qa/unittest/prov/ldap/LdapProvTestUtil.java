// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.qa.unittest.prov.ProvTestUtil;

class LdapProvTestUtil extends ProvTestUtil {

    LdapProvTestUtil() throws Exception {
        super(LdapProv.getInst());
    }
    
    LdapProv getProv() {
        return (LdapProv) prov;
    }
}

