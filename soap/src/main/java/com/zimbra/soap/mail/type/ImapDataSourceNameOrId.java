// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class ImapDataSourceNameOrId extends DataSourceNameOrId {

    public static ImapDataSourceNameOrId createForName(String name) {
        ImapDataSourceNameOrId obj = new ImapDataSourceNameOrId();
        obj.setName(name);
        return obj;
    }

    public static ImapDataSourceNameOrId createForId(String id) {
        ImapDataSourceNameOrId obj = new ImapDataSourceNameOrId();
        obj.setId(id);
        return obj;
    }
}
