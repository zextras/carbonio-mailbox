// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Set;

import com.zimbra.common.service.ServiceException;

public interface AliasedEntry {

    /*
     * entry with aliases
     */
    
    /**
     * returns whether addr is the entry's primary address or 
     * one of the aliases.
     * 
     * @param addr
     * @return
     */
    boolean isAddrOfEntry(String addr);
    
    String[] getAliases() throws ServiceException;
    
    /**
     * returns all addresses of the entry, including primary address and 
     * all aliases.
     * 
     * @return
     */
    Set<String> getAllAddrsSet();
}
