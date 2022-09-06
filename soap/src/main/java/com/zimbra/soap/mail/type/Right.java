// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/*
 * Delete this class in bug 66989
 */

@XmlAccessorType(XmlAccessType.NONE)
public class Right {

  /**
   * @zm-api-field-tag right-name
   * @zm-api-field-description Name for right
   */
  @XmlAttribute(name = MailConstants.A_RIGHT /* right */, required = true)
  private final String right;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private Right() {
    this((String) null);
  }

  public Right(String right) {
    this.right = right;
  }

  public String getRight() {
    return right;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("right", right);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
