// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_MODIFY_PROFILE_IMAGE_RESPONSE)
public class ModifyProfileImageResponse {
  /**
   * @zm-api-field-tag itemId
   * @zm-api-field-description Item ID of profile image
   */
  @XmlAttribute(name = MailConstants.A_ITEMID /* itemId */, required = false)
  private int itemId;

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }
}
