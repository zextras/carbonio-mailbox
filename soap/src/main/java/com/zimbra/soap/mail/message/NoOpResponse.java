// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_NO_OP_RESPONSE)
public final class NoOpResponse {

  /**
   * @zm-api-field-tag wait-disallowed
   * @zm-api-field-description Set if wait was disallowed
   */
  @XmlAttribute(name = MailConstants.A_WAIT_DISALLOWED /* waitDisallowed */, required = false)
  private ZmBoolean waitDisallowed;

  public NoOpResponse() {}

  public NoOpResponse(Boolean waitDisallowed) {
    setWaitDisallowed(waitDisallowed);
  }

  public static NoOpResponse create(Boolean waitDisallowed) {
    return new NoOpResponse(waitDisallowed);
  }

  public void setWaitDisallowed(Boolean waitDisallowed) {
    this.waitDisallowed = ZmBoolean.fromBool(waitDisallowed);
  }

  public Boolean getWaitDisallowed() {
    return ZmBoolean.toBool(waitDisallowed);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("waitDisallowed", waitDisallowed);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
