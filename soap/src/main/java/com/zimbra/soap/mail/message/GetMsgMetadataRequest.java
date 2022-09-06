// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.IdsAttr;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get message metadata
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_MSG_METADATA_REQUEST)
public class GetMsgMetadataRequest {

  /**
   * @zm-api-field-description Messages selector
   */
  @XmlElement(name = MailConstants.E_MSG /* m */, required = true)
  private final IdsAttr msgIds;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetMsgMetadataRequest() {
    this((IdsAttr) null);
  }

  public GetMsgMetadataRequest(IdsAttr msgIds) {
    this.msgIds = msgIds;
  }

  public IdsAttr getMsgIds() {
    return msgIds;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("msgIds", msgIds);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
