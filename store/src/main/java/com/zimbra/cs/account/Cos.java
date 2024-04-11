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

import com.zimbra.common.service.ServiceException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author schemers
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Cos extends ZAttrCos {
 
    private Map<String, Object> mAccountDefaults = new HashMap<>();

    public Cos(String name, String id, Map<String,Object> attrs, Provisioning prov) {
        super(name, id, attrs, prov);
        resetData();
    }
    
    @Override
    public EntryType getEntryType() {
        return EntryType.COS;
    }

    public void modify(Map<String, Object> attrs) throws ServiceException {
        getProvisioning().modifyAttrs(this, attrs);
    }

    public Cos copyCos(String destCosName) throws ServiceException {
        return getProvisioning().copyCos(getId(), destCosName);
    }

    public void renameCos(String newName) throws ServiceException {
        getProvisioning().renameCos(getId(), newName);
    }

    public void deleteCos() throws ServiceException {
        getProvisioning().deleteCos(getId());
    }

    @Override
    protected void resetData() {
        super.resetData();
        try {
            getDefaults(AttributeFlag.accountInherited, mAccountDefaults);
        } catch (ServiceException e) {
            // TODO log
        }
    }

    public Map<String, Object> getAccountDefaults() {
        return mAccountDefaults;
    }
    
    public boolean isDefaultCos() {
        return getName().equals(Provisioning.DEFAULT_COS_NAME) ||
                getName().equals(Provisioning.DEFAULT_EXTERNAL_COS_NAME);
    }
}
