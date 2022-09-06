// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.VoiceConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceMailCallParty {

  /**
   * @zm-api-field-tag address-type
   * @zm-api-field-description Type of the address in <b>{personal-name}</b> and
   *     <b>{phone-number}</b> <br>
   *     The supported values are '<b>f</b>'(rom) or '<b>t</b>'(o) (for voice mails it should always
   *     be '<b>f</b>' because we only return caller info)
   */
  @XmlAttribute(name = MailConstants.A_ADDRESS_TYPE /* t */, required = true)
  private String addressType;

  /**
   * @zm-api-field-tag personal-name
   * @zm-api-field-description Personal name
   */
  @XmlAttribute(name = MailConstants.A_PERSONAL /* p */, required = true)
  private String personalName;

  /**
   * @zm-api-field-tag phone-number
   * @zm-api-field-description Phone number
   */
  @XmlAttribute(name = VoiceConstants.A_PHONENUM /* n */, required = true)
  private String phoneNumber;

  public VoiceMailCallParty() {}

  public void setAddressType(String addressType) {
    this.addressType = addressType;
  }

  public void setPersonalName(String personalName) {
    this.personalName = personalName;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getAddressType() {
    return addressType;
  }

  public String getPersonalName() {
    return personalName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("addressType", addressType)
        .add("personalName", personalName)
        .add("phoneNumber", phoneNumber);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
