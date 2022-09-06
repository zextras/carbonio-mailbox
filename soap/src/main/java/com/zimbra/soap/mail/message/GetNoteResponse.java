// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.NoteInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_NOTE_RESPONSE)
public class GetNoteResponse {

  /**
   * @zm-api-field-description Note information
   */
  @XmlElement(name = MailConstants.E_NOTE /* note */, required = true)
  private final NoteInfo note;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetNoteResponse() {
    this((NoteInfo) null);
  }

  public GetNoteResponse(NoteInfo note) {
    this.note = note;
  }

  public NoteInfo getNote() {
    return note;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("note", note);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
