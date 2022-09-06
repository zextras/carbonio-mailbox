// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class InheritedFlaggedValue {

  /**
   * @zm-api-field-tag inherited-flag
   * @zm-api-field-description Inherited flag
   *     <table>
   * <tr> <td> <b>1 (true)</b> </td> <td> inherited from a group </td> </tr>
   * <tr> <td> <b>0 (false)</b> </td> <td> set directly on the entry </td> </tr>
   * </table>
   */
  @XmlAttribute(name = AdminConstants.A_INHERITED /* inherited */, required = true)
  private final ZmBoolean inherited;

  /**
   * @zm-api-field-tag value
   * @zm-api-field-description Value
   */
  @XmlValue private final String value;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private InheritedFlaggedValue() {
    this(false, (String) null);
  }

  public InheritedFlaggedValue(boolean inherited, String value) {
    this.inherited = ZmBoolean.fromBool(inherited);
    this.value = value;
  }

  public boolean getInherited() {
    return ZmBoolean.toBool(inherited);
  }

  public String getValue() {
    return value;
  }
}
