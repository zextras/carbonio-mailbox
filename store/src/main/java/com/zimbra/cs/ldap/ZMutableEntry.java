// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import java.util.Map;
import java.util.Set;

/**
 * Represents a transient, mutable entry in memory.
 *  
 * @author pshao
 */
public abstract class ZMutableEntry extends ZEntry {

    /**
     * Sets the provided attribute to this entry. 
     * If this entry already contains an attribute with the same name, 
     * then the current value will be *replaced* by the new value.
     */
    public abstract void setAttr(String attrName, String value);
    
    /**
     * Adds the provided attribute to this entry. 
     * If this entry already contains an attribute with the same name, 
     * then their values will be *merged*.
     */
    public abstract void addAttr(String attrName, Set<String> values);
    
    /**
     * Adds the provided attribute to this entry. 
     * If this entry already contains an attribute with the same name, 
     * then their values will be *merged*.
     */
    public abstract void addAttr(String attrName, String value);
    
    public abstract String getAttrString(String attrName) throws LdapException;
    
    public abstract boolean hasAttribute(String attrName);

    
    /**
     * take a map (key = String, value = String | String[]) and populate ZAttributes.
     */
    public abstract void mapToAttrs(Map<String, Object> mapAttrs);
    
    public abstract void setDN(String dn);
    
}
