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

@XmlAccessorType(XmlAccessType.NONE)
public class ParentId {

  /**
   * @zm-api-field-tag item-id-of-parent
   * @zm-api-field-description Item ID of parent
   */
  @XmlAttribute(name = MailConstants.A_PARENT_ID /* parentId */, required = true)
  private final String parentId;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ParentId() {
    this((String) null);
  }

  public ParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getParentId() {
    return parentId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("parentId", parentId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
