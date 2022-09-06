// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_GCM_SENDER_ID_RESPONSE)
public class GetGcmSenderIdResponse {

  /**
   * @zm-api-field-tag Sender Id
   * @zm-api-field-description Sender Id required by Android client to register for push
   *     notifications
   */
  @XmlElement(name = AccountConstants.E_GCM_SENDER_ID, required = true)
  private final String senderId;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetGcmSenderIdResponse() {
    this(null);
  }

  public GetGcmSenderIdResponse(String senderId) {
    this.senderId = senderId;
  }

  public String getSenderId() {
    return senderId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("senderId", senderId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
