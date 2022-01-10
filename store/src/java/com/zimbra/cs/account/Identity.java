// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.mime.shim.JavaMailInternetAddress;

/**
 * @author schemers
 */
public class Identity extends AccountProperty implements Comparable {

    public Identity(Account acct, String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(acct, name, id, attrs, null, prov);
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.IDENTITY;
    }

    /**
     * this should only be used internally by the server. it doesn't modify the real id, just
     * the cached one.
     * @param id
     */
    public void setId(String id) {
        mId = id;
        getRawAttrs().put(Provisioning.A_zimbraPrefIdentityId, id);
    }

    public InternetAddress getFriendlyEmailAddress() {
        String personalPart = getAttr(Provisioning.A_zimbraPrefFromDisplay);
        if (personalPart == null || personalPart.trim().equals(""))
            personalPart = null;
        String address = getAttr(Provisioning.A_zimbraPrefFromAddress);

        try {
            return new JavaMailInternetAddress(address, personalPart, MimeConstants.P_CHARSET_UTF8);
        } catch (UnsupportedEncodingException e) { }

        // UTF-8 should *always* be supported (i.e. this is actually unreachable)
        try {
            // fall back to using the system's default charset (also pretty much guaranteed not to be "unsupported")
            return new JavaMailInternetAddress(address, personalPart);
        } catch (UnsupportedEncodingException e) { }

        // if we ever reached this point (which we won't), just return an address with no personal part
        InternetAddress ia = new JavaMailInternetAddress();
        ia.setAddress(address);
        return ia;
    }
}
