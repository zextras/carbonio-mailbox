// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_DESTROY_WAIT_SET_RESPONSE)
public class DestroyWaitSetResponse {

  /**
   * @zm-api-field-tag waitset-id
   * @zm-api-field-description WaitSet ID
   */
  @XmlAttribute(name = MailConstants.A_WAITSET_ID /* waitSet */, required = true)
  private final String waitSetId;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DestroyWaitSetResponse() {
    this((String) null);
  }

  public DestroyWaitSetResponse(String waitSetId) {
    this.waitSetId = waitSetId;
  }

  public String getWaitSetId() {
    return waitSetId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("waitSetId", waitSetId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
