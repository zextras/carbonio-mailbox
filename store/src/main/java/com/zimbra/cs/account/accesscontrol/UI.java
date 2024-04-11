// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;

public class UI implements Comparable<UI> {
    
    private String name;
    private String desc;
    
    public UI(String name) {
        this.name = name;
    }
    
    /*
     * for sorting for RightManager.genAdminDocs()
     */
    @Override
    public int compareTo(UI other) {
        return name.compareTo(other.name);
    }
    
    String getName() {
        return name;
    }
    
    void setDesc(String desc) {
        this.desc = desc;
    }
    
    String getDesc() {
        return desc;
    }
    
    void validate() throws ServiceException {
        if (desc == null) {
            throw ServiceException.PARSE_ERROR("missing desc", null);
        }
    }


    
}
