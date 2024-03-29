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

import java.util.Map;

public abstract class NamedEntry extends Entry implements Comparable {

    protected String mName;
    protected String mId;

    public interface Visitor  {
        void visit(NamedEntry entry) throws ServiceException;
    }
    
    public interface CheckRight {
        boolean allow(NamedEntry entry) throws ServiceException;
    }

    protected NamedEntry(String name, String id, Map<String, Object> attrs, Map<String, Object> defaults, Provisioning prov) {
        super(attrs, defaults, prov);
        mName = name;
        mId = id;
    }

    public String getLabel() {
        return getName();
    }
    
    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public int compareTo(Object obj) {
        if (!(obj instanceof NamedEntry))
            return 0;
        NamedEntry other = (NamedEntry) obj;
        return getName().compareTo(other.getName());
    }
    
    public synchronized String toString() {
        return String.format("[%s %s]", getClass().getName(), getName());
    }

}
