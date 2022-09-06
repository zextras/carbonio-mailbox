// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class AdminObjectInfo implements AdminObjectInterface {

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Name
   */
  @XmlAttribute(name = AdminConstants.A_NAME /* name */, required = true)
  private final String name;

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = AdminConstants.A_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-description Attributes
   */
  @XmlElement(name = AdminConstants.E_A /* a */, required = false)
  private final List<Attr> attrList;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private AdminObjectInfo() {
    this(null, null, null);
  }

  public AdminObjectInfo(String id, String name, Collection<Attr> attrs) {
    this.name = name;
    this.id = id;
    this.attrList = new ArrayList<Attr>();
    if (attrs != null) {
      this.attrList.addAll(attrs);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public List<Attr> getAttrList() {
    return Collections.unmodifiableList(attrList);
  }
}
