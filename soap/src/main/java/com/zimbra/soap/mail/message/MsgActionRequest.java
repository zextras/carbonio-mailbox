// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import com.zimbra.soap.mail.type.ActionSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Perform an action on a message <br>
 *     For op="update", caller can specify any or all of: l="{folder}", name="{name}",
 *     color="{color}", tn="{tag-names}", f="{flags}". <br>
 *     <br>
 *     For op="!spam", can optionally specify a destination folder
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_MSG_ACTION_REQUEST)
public class MsgActionRequest {

  /**
   * @zm-api-field-description Specify the action to perform
   */
  @ZimbraUniqueElement
  @XmlElement(name = MailConstants.E_ACTION /* action */, required = true)
  private final ActionSelector action;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private MsgActionRequest() {
    this((ActionSelector) null);
  }

  public MsgActionRequest(ActionSelector action) {
    this.action = action;
  }

  public ActionSelector getAction() {
    return action;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("action", action).toString();
  }
}
