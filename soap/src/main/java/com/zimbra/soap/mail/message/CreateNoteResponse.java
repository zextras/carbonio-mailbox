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
@XmlRootElement(name = MailConstants.E_CREATE_NOTE_RESPONSE)
public class CreateNoteResponse {

  /**
   * @zm-api-field-description Details of the created note
   */
  @XmlElement(name = MailConstants.E_NOTE, required = false)
  private final NoteInfo note;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CreateNoteResponse() {
    this((NoteInfo) null);
  }

  public CreateNoteResponse(NoteInfo note) {
    this.note = note;
  }

  public NoteInfo getNote() {
    return note;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("note", note).toString();
  }
}
