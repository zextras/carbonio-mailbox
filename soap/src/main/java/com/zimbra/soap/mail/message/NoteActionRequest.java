// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.NoteActionSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Perform an action on an note
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_NOTE_ACTION_REQUEST)
public class NoteActionRequest {

  /**
   * @zm-api-field-description Specify the action to perform
   */
  @XmlElement(name = MailConstants.E_ACTION, required = true)
  private final NoteActionSelector action;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private NoteActionRequest() {
    this((NoteActionSelector) null);
  }

  public NoteActionRequest(NoteActionSelector action) {
    this.action = action;
  }

  public NoteActionSelector getAction() {
    return action;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("action", action).toString();
  }
}
