// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class RssDataSourceNameOrId extends DataSourceNameOrId {
  public static RssDataSourceNameOrId createForName(String name) {
    RssDataSourceNameOrId obj = new RssDataSourceNameOrId();
    obj.setName(name);
    return obj;
  }

  public static RssDataSourceNameOrId createForId(String id) {
    RssDataSourceNameOrId obj = new RssDataSourceNameOrId();
    obj.setId(id);
    return obj;
  }
}
