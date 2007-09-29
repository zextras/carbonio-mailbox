/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2005, 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.account.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.object.ObjectType;
import com.zimbra.cs.zimlet.ZimletHandler;
import com.zimbra.cs.zimlet.ZimletUtil;

class LdapZimlet extends Zimlet implements LdapEntry, ObjectType {

    private String mDn;
    
	public LdapZimlet(String dn, Attributes attrs) throws NamingException {
        super(LdapUtil.getAttrString(attrs, Provisioning.A_cn),
                LdapUtil.getAttrString(attrs, Provisioning.A_cn),                 
                LdapUtil.getAttrs(attrs));
        mDn = dn;
	}
	
    public String getType() {
        return getAttr(Provisioning.A_cn);
    }
    
    public String getDescription() {
        return getAttr(Provisioning.A_zimbraZimletDescription);
    }
    
    public boolean isIndexingEnabled() {
        return getBooleanAttr(Provisioning.A_zimbraZimletIndexingEnabled, false);
    }
    
    public boolean isStoreMatched() {
        return false;
    }
    
    public String getHandlerClassName() {
        return getAttr(Provisioning.A_zimbraZimletHandlerClass);
    }
    
    public ZimletHandler getHandler() {
    	return ZimletUtil.getHandler(getName());
    }
    
    public String getHandlerConfig() {
        return getAttr(Provisioning.A_zimbraZimletHandlerConfig);
    }

    public String getServerIndexRegex() {
    	return getAttr(Provisioning.A_zimbraZimletServerIndexRegex);
    }

    public String getDN() {
        return mDn;
    }
}
