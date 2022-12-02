// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import java.util.List;

import com.zimbra.common.service.ServiceException;

// TODO deprecate after all legacy stuff is remvoed
public interface IAttributes {
    
    /**
     * - If a method does not have a CheckBinary parameter, it will *not* check 
     *   for binary data and binary transfer based on AttributeManager.
     *   It will assume all attributes are *not* binary.
     *   
     * - If a method has a CheckBinary parameter, it will check for binary data 
     *   and binary transfer based on AttributeManager if CheckBinary is CHECK.
     *   It will assume all attributes are *not* binary if CheckBinary is NOCHECK.
     */
    
    
    public String getAttrString(String attrName) throws ServiceException;
    
    public String[] getMultiAttrString(String attrName) throws ServiceException;
    
    public String[] getMultiAttrString(String attrName, 
            boolean containsBinaryData, boolean isBinaryTransfer) throws ServiceException;
    
    
    public static enum CheckBinary {
        CHECK,
        NOCHECK;
    }
    
    public List<String> getMultiAttrStringAsList(String attrName, CheckBinary checkBinary) 
    throws ServiceException;
    
    /**
     * Whether the specified attribute is present.
     * 
     * @param attrName
     * @return
     */
    public abstract boolean hasAttribute(String attrName);
    
    /**
     * Whether it contains the specified attribute with the specified value.
     *  
     * @param attrName
     * @param value
     * @return
     */
    public abstract boolean hasAttributeValue(String attrName, String value);

}
