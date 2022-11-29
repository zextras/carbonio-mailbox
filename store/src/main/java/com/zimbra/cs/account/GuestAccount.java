// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/**
 *
 */
package com.zimbra.cs.account;

import java.util.HashMap;
import java.util.Map;


public class GuestAccount extends Account {
    private final String digest;     // for guest grantee
    private String accessKey;  // for key grantee
    
    /**email address for anonymous account */
    public static final String EMAIL_ADDRESS_PUBLIC = "public";

    public static final Account ANONYMOUS_ACCT = new GuestAccount(EMAIL_ADDRESS_PUBLIC, null);

    /** The pseudo-GUID signifying "all authenticated and unauthenticated users". */
    public static final String GUID_PUBLIC   = "99999999-9999-9999-9999-999999999999";

    /** The pseudo-GUID signifying "all authenticated users". */
    public static final String GUID_AUTHUSER = "00000000-0000-0000-0000-000000000000";

    private static Map<String, Object> getAnonAttrs() {
        Map<String,Object> attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_uid, "public");
        attrs.put(Provisioning.A_zimbraId, GuestAccount.GUID_PUBLIC);
        return attrs;
    }

    public GuestAccount(String emailAddress, String password) {
        super(emailAddress, GuestAccount.GUID_PUBLIC, getAnonAttrs(), null, null);
        digest = AuthToken.generateDigest(emailAddress, password);
    }

    public GuestAccount(AuthToken auth) {
        // for key grantee type, sometimes there could be no email address
        super(auth.getExternalUserEmail()==null?"":auth.getExternalUserEmail(), GuestAccount.GUID_PUBLIC, getAnonAttrs(), null, null);
        digest = auth.getDigest();
        accessKey = auth.getAccessKey();
    }

    public boolean matches(String emailAddress, String password) {
        if (getName().compareToIgnoreCase(emailAddress) != 0)
            return false;
        String matchDigest = AuthToken.generateDigest(
                getName() /* use getName() instead of emailAddress so that case doesn't affect result */, password);
        return (this.digest.compareTo(matchDigest) == 0);
    }

    public boolean matchesAccessKey(String emailAddress, String accesskey) {
        /* do not verify emailAddress for key grantees
        if (getName().compareTo(emailAddress) != 0)
            return false;
        */
        if (accessKey == null)
            return false;
        return (accessKey.compareTo(accesskey) == 0);
    }

    public String getDigest() {
        return digest;
    }

    public String getAccessKey() {
        return accessKey;
    }

    @Override
    public boolean isIsExternalVirtualAccount() {
        return !EMAIL_ADDRESS_PUBLIC.equals(getName()) && getAccessKey() == null;
    }

    @Override
    public String getExternalUserMailAddress() {
        return isIsExternalVirtualAccount() ? getName() : null;
    }
}