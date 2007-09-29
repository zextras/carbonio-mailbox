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
package com.zimbra.cs.im;

import org.xmpp.packet.JID;

public class IMAddr {
    private String mAddr;
    
    public IMAddr(String addr) {
        mAddr = addr;
    }
    
    public String getAddr() { return mAddr; }
    
    public String toString() { return mAddr; }
    
    public JID makeJID() {
        int domainSplit = mAddr.indexOf('@');
        
        if (domainSplit > 0) {
            String namePart = mAddr.substring(0, domainSplit);
            String domainPart = mAddr.substring(domainSplit+1);
            return new JID(namePart, domainPart, "");
        } else {
            return new JID(mAddr);
        }            
    }
    
    public static IMAddr fromJID(JID jid) {
        if (jid.getDomain() != null && jid.getDomain().length() > 0)
            return new IMAddr(jid.getNode() + '@' + jid.getDomain());
        else
            return new IMAddr(jid.getNode());
    }
    
    public boolean equals(Object other) {
        return (((IMAddr)other).mAddr).equals(mAddr);
    }
    
    public int hashCode() {
        return mAddr.hashCode();
    }
}
