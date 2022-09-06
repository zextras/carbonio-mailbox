// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.ZimletConstants;
import com.zimbra.soap.base.ZimletIncludeCSS;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Implemented as an object rather than using String with @XmlElement because when constructing a
 * JAXB object containing this and other "Strings" there needs to be a way of differentiating them
 * when marshaling to XML.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = ZimletConstants.ZIMLET_TAG_CSS)
public class AdminZimletIncludeCSS implements ZimletIncludeCSS {

  /**
   * @zm-api-field-description Included Cascading Style Sheet (CSS)
   */
  @XmlValue private String value;

  @SuppressWarnings("unused")
  private AdminZimletIncludeCSS() {}

  public AdminZimletIncludeCSS(String value) {
    setValue(value);
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("value", value);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
