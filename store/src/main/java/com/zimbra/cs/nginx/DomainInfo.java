/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.nginx;

/*
 * virtual IP to domain name mapping
 */
class DomainInfo extends LookupEntry {
    
    private String mDomainName;
    
    DomainInfo(String virtualIP, String domainName) {
        super(virtualIP);
        mDomainName = domainName;
    }
    
    String getDomainName() {
        return mDomainName;
    }
}
