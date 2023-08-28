// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zookeeper;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("details")
public class Service {
    private String id;

    public Service() {
        this("");
    }

    public Service(String id) {
        this.id = id;
    }

    public void setService(String id) {
        this.id = id;
    }

    public String getService() {
        return id;
    }
}
