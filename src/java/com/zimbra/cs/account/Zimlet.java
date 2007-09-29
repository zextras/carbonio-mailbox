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
package com.zimbra.cs.account;

import java.util.Map;

public class Zimlet extends NamedEntry {
	protected Zimlet(String name, String id, Map<String, Object> attrs) {
        super(name, id, attrs, null);
    }

    public boolean isEnabled() {
        return getBooleanAttr(Provisioning.A_zimbraZimletEnabled, false);
    }
    
    public String getPriority() {
        return getAttr(Provisioning.A_zimbraZimletPriority);
    }
    
    public boolean isExtension() {
        return getBooleanAttr(Provisioning.A_zimbraZimletIsExtension, false);
    }
}
