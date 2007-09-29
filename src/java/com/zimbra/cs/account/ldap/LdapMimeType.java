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

/*
 * Created on Apr 14, 2005
 *
 */
package com.zimbra.cs.account.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.MimeTypeInfo;

/**
 * @author kchen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class LdapMimeType extends Entry implements LdapEntry, MimeTypeInfo {

    private String mDn;
    
    /**
     * @param dn
     * @param attrs
     * @throws NamingException 
     */
    LdapMimeType(String dn, Attributes attrs) throws NamingException {
        super(LdapUtil.getAttrs(attrs), null);
        mDn = dn;
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.mime.MimeTypeInfo#getMimeType()
     */
    public String getType() {
        return super.getAttr(Provisioning.A_zimbraMimeType);
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.mime.MimeTypeInfo#getHandlerClass()
     */
    public String getHandlerClass() {
        return super.getAttr(Provisioning.A_zimbraMimeHandlerClass, null);
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.mime.MimeTypeInfo#isIndexingEnabled()
     */
    public boolean isIndexingEnabled() {
        return super.getBooleanAttr(Provisioning.A_zimbraMimeIndexingEnabled, true);
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.mime.MimeTypeInfo#getDescription()
     */
    public String getDescription() {
        return super.getAttr(Provisioning.A_description, "");
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.mime.MimeTypeInfo#getFileExtensions()
     */
    public String[] getFileExtensions() {
        return super.getMultiAttr(Provisioning.A_zimbraMimeFileExtension);
    }

    public String getExtension() {
        return super.getAttr(Provisioning.A_zimbraMimeHandlerExtension, null);
    }

    public String getDN() {
        return mDn;
    }

}
