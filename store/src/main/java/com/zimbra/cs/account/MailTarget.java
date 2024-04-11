// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;

public abstract class MailTarget extends NamedEntry {
    
    protected String mDomain;
    protected String mUnicodeDomain;
    protected String mUnicodeName;
    
    private static final String NO_DOMAIN_ID = "";
    
    public MailTarget(String name, String id, Map<String,Object> attrs, Map<String, Object> defaults, Provisioning prov) {
        super(name, id, attrs, defaults, prov);
        int index = name.indexOf('@');
        if (index != -1)  {
            String local = name.substring(0, index);
            mDomain = name.substring(index+1);
            mUnicodeDomain = IDNUtil.toUnicodeDomainName(mDomain);
            mUnicodeName = local + "@" + mUnicodeDomain;
        } else
            mUnicodeName = name;
    }

    /**
     * @return the domain name for this account (foo.com), or null if an admin account. 
     */
    public String getDomainName() {
        return mDomain;
    }
    
    public String getUnicodeDomainName() {
        return mUnicodeDomain;
    }
    
    public String getUnicodeName() {
        return mUnicodeName;
    }
    
    public String getDomainId() {
        String domainId = (String)getCachedData(EntryCacheDataKey.MAILTARGET_DOMAIN_ID);
        
        if (domainId == null) {
            try {
                String dname = getDomainName();
                Provisioning prov = getProvisioning();
                Domain domain =  dname == null ? null : (prov == null ? null : prov.get(Key.DomainBy.name, dname));
                if (domain != null)
                    domainId = domain.getId();
            } catch (ServiceException e) {
                ZimbraLog.account.warn("unable to get domain id for domain " + getDomainName() , e);
            }
            
            if (domainId == null)
                domainId = NO_DOMAIN_ID; // set it to a non-null string so we don't repeatedly look up the domain
            
            setCachedData(EntryCacheDataKey.MAILTARGET_DOMAIN_ID, domainId);
        }
        
        // OK and more efficient to use == instead of equal
        if (domainId == NO_DOMAIN_ID)
            return null;
        else
            return domainId;
    }

}
