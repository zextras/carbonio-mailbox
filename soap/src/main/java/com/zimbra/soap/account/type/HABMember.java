// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public abstract class HABMember {

  /**
   * @zm-api-field-tag member-email-address
   * @zm-api-field-description HAB Member name - an email address (user@domain)
   */
  @ZimbraJsonAttribute
  @XmlElement(name = AccountConstants.A_NAME /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-tag seniorityIndex
   * @zm-api-field-description seniorityIndex of the HAB group member
   */
  @XmlAttribute(
      name = AccountConstants.A_HAB_SENIORITY_INDEX /* seniorityIndex */,
      required = false)
  private int seniorityIndex;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  public HABMember() {
    this((String) null);
  }

  public HABMember(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getSeniorityIndex() {
    return seniorityIndex;
  }

  public void setSeniorityIndex(int seniorityIndex) {
    this.seniorityIndex = seniorityIndex;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper.add("name", name);
    if (seniorityIndex != 0) {
      helper.add("seniorityIndex", seniorityIndex);
    }
    return helper;
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
