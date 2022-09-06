// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public class DataSourceNameOrId extends NameOrId {
  public static DataSourceNameOrId createForName(String name) {
    DataSourceNameOrId obj = new DataSourceNameOrId();
    obj.setName(name);
    return obj;
  }

  public static DataSourceNameOrId createForId(String id) {
    DataSourceNameOrId obj = new DataSourceNameOrId();
    obj.setId(id);
    return obj;
  }
}
