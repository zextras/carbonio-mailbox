// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import org.apache.jsieve.mail.MailAdapter.Address;

import com.zimbra.common.util.EmailUtil;

/**
 * Used for returning an email address to the jSieve
 * address test. 
 */
class FilterAddress implements Address {
    
    static final Address[] EMPTY_ADDRESS_ARRAY = new Address[0];
    
    private String mLocalPart;
    private String mDomain;
    
    FilterAddress(String address) {
        String[] parts = EmailUtil.getLocalPartAndDomain(address);
        if (parts != null) {
            mLocalPart = parts[0];
            mDomain = parts[1];
        }
    }

    public String getLocalPart() {
        return mLocalPart;
    }

    public String getDomain() {
        return mDomain;
    }
}
