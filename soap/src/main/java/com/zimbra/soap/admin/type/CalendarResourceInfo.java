// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class CalendarResourceInfo extends AdminObjectInfo {

  /** no-argument constructor wanted by JAXB */
  private CalendarResourceInfo() {
    super(null, null, null);
  }

  public CalendarResourceInfo(String id, String name) {
    super(id, name, null);
  }

  public CalendarResourceInfo(String id, String name, Collection<Attr> attrs) {
    super(id, name, attrs);
  }
}
