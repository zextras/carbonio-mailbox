// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;

public class ShareLocator extends ZAttrShareLocator {

    public ShareLocator(String id, Map<String, Object> attrs, Provisioning prov) {
        super(id, attrs, prov);
    }

    public String getUuid() {
        return getCn();
    }
}
