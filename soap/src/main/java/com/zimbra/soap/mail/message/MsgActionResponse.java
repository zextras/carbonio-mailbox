// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import com.zimbra.soap.mail.type.ActionResult;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_MSG_ACTION_RESPONSE)
public class MsgActionResponse {

  /**
   * @zm-api-field-description The <b>&lt;action></b> element in the response always contains the
   *     same id list that the client sent in the request. In particular, IDs that were ignored due
   *     to constraints are included in the id list.
   */
  @ZimbraUniqueElement
  @XmlElement(name = MailConstants.E_ACTION /* action */, required = true)
  private final ActionResult action;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private MsgActionResponse() {
    this((ActionResult) null);
  }

  public MsgActionResponse(ActionResult action) {
    this.action = action;
  }

  public ActionResult getAction() {
    return action;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("action", action).toString();
  }
}
