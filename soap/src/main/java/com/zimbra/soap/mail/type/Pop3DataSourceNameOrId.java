// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class Pop3DataSourceNameOrId extends DataSourceNameOrId {
    public static Pop3DataSourceNameOrId createForName(String name) {
        Pop3DataSourceNameOrId obj = new Pop3DataSourceNameOrId();
        obj.setName(name);
        return obj;
    }

    public static Pop3DataSourceNameOrId createForId(String id) {
        Pop3DataSourceNameOrId obj = new Pop3DataSourceNameOrId();
        obj.setId(id);
        return obj;
    }
}
