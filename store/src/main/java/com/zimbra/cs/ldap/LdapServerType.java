// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

public enum LdapServerType {
    MASTER,
    REPLICA;
    
    public boolean isMaster() {
        return this == MASTER;
    }
    
    // Convenient method to bridge the legacy API
    public static LdapServerType get(boolean master) {
        return master ? LdapServerType.MASTER : LdapServerType.REPLICA;
    }
}
