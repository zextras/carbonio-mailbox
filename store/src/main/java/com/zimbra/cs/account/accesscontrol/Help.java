// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.util.List;

import com.google.common.collect.Lists;
import com.zimbra.common.service.ServiceException;

public class Help {
    private String name;
    private String desc;
    private List<String> items = Lists.newArrayList();
    
    Help(String name) {
        this.name = name;
    }
    
    String getName() {
        return name;
    }
    
    void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getDesc() {
        return desc;
    }

    void addItem(String item) {
        items.add(item);
    }
    
    public List<String> getItems() {
        return items;
    }
    
    void validate() throws ServiceException {
        if (desc == null) {
            throw ServiceException.PARSE_ERROR("missing desc", null);
        }
    }

}
