// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Sep 23, 2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.cs.account;

import com.zimbra.common.account.ZAttrProvisioning.DomainStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * @author schemers
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Domain extends ZAttrDomain {
    private final String mUnicodeName;
    private final Map<String, Object> mAccountDefaults = new HashMap<>();
    
    public Domain(String name, String id, Map<String, Object> attrs, Map<String, Object> defaults, Provisioning prov) {
        super(name, id, attrs, defaults, prov);
        if (name == null)
            mUnicodeName = name;
        else
            mUnicodeName = IDNUtil.toUnicodeDomainName(name);
        if (attrs != null)
            resetData();
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.DOMAIN;
    }
    
    public void modify(Map<String, Object> attrs) throws ServiceException {
        getProvisioning().modifyAttrs(this, attrs);
    }

	public String getGalSearchBase(String searchBaseSpec) throws ServiceException {
        throw ServiceException.FAILURE("unsupported", null);
    }

    @Override
    protected void resetData() {
        super.resetData();
        try {
            getDefaults(AttributeFlag.accountCosDomainInherited, mAccountDefaults);
        } catch (ServiceException e) {
            // TODO log
        }
    }
    
    public Map<String, Object> getAccountDefaults() {
        return mAccountDefaults;
    }
    
    public String getUnicodeName() {
        return mUnicodeName;
    }

    public boolean isSuspended() {
        DomainStatus status = getDomainStatus();
        boolean suspended = status != null && status.isSuspended();

        if (suspended)
            ZimbraLog.account.warn("domain " + mName + " is " + Provisioning.DOMAIN_STATUS_SUSPENDED);
        return suspended;
    }
    
    public boolean isShutdown() {
        DomainStatus status = getDomainStatus();
        boolean shutdown = status != null && status.isShutdown();
        
        if (shutdown)
            ZimbraLog.account.warn("domain " + mName + " is " + Provisioning.DOMAIN_STATUS_SHUTDOWN);
        return shutdown;
    }
    
    public boolean beingRenamed() {
        String renameInfo = getAttr(Provisioning.A_zimbraDomainRenameInfo);
        return (!StringUtil.isNullOrEmpty(renameInfo));
    }
    
    public boolean isLocal() {
        Provisioning.DomainType domainType = getDomainType();
        return Provisioning.DomainType.local == domainType;
    }

    

    public List getAllAccounts() throws ServiceException {
            return getProvisioning().getAllAccounts(this);
    }
}
