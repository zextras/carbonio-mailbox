// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_UC_SERVICE)
@XmlType(propOrder = {})
public class UCServiceInfo extends AdminObjectInfo {

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private UCServiceInfo() {
    this(null, null, null);
  }

  public UCServiceInfo(String id, String name) {
    this(id, name, null);
  }

  public UCServiceInfo(String id, String name, Collection<Attr> attrs) {
    super(id, name, attrs);
  }
}
