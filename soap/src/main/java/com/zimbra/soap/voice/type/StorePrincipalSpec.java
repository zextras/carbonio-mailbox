// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.VoiceConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class StorePrincipalSpec {

  /**
   * @zm-api-field-tag id-of-user-in-the-backing-store
   * @zm-api-field-description ID of user in the backing store
   */
  @XmlAttribute(name = VoiceConstants.A_ID /* id */, required = false)
  private String id;

  /**
   * @zm-api-field-tag name-of-user-in-the-backing-store
   * @zm-api-field-description Name of user in the backing store
   */
  @XmlAttribute(name = VoiceConstants.A_NAME /* name */, required = false)
  private String name;

  /**
   * @zm-api-field-tag account-number
   * @zm-api-field-description Account Number
   */
  @XmlAttribute(name = VoiceConstants.A_ACCOUNT_NUMBER /* accountNumber */, required = false)
  private String accountNumber;

  public StorePrincipalSpec() {}

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("name", name).add("accountNumber", accountNumber);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
