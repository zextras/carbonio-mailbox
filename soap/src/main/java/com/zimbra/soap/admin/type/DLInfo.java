// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_COS)
@XmlType(propOrder = {})
public class DLInfo extends AdminObjectInfo {

  /**
   * @zm-api-field-tag group-is-dynamic
   * @zm-api-field-description Flags whether a group is dynamic or not
   */
  @XmlAttribute(name = AdminConstants.A_DYNAMIC, required = false)
  private ZmBoolean dynamic;

  /**
   * @zm-api-field-tag via-dl-name
   * @zm-api-field-description Present if the account is a member of the returned list because they
   *     are either a direct or indirect member of another list that is a member of the returned
   *     list. For example, if a user is a member of engineering@domain.com, and
   *     engineering@domain.com is a member of all@domain.com, then
   *     <pre>
   *     &lt;dl name="all@domain.com" ... via="engineering@domain.com"/>
   * </pre>
   *     would be returned.
   */
  @XmlAttribute(name = AdminConstants.A_VIA, required = true)
  private final String via;
  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DLInfo() {
    this(null, null, null, null);
  }

  public DLInfo(String id, String name) {
    this(id, name, null, null);
  }

  public DLInfo(String id, String name, Collection<Attr> attrs) {
    this(id, name, null, attrs);
  }

  public DLInfo(String id, String name, String via, Collection<Attr> attrs) {
    super(id, name, attrs);
    this.via = via;
  }

  public String getVia() {
    return via;
  }

  public Boolean isDynamic() {
    return ZmBoolean.toBool(dynamic, false);
  }
}
