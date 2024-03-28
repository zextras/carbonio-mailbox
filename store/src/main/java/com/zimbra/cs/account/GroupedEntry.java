// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;

public interface GroupedEntry {
    
    /**
     * returns all addresses of this entry that can be identified as a member in a 
     * static group.
     * 
     * @return
     */
    String[] getAllAddrsAsGroupMember() throws ServiceException;
}
