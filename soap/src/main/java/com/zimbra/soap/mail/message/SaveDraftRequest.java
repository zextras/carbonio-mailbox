// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.SaveDraftMsg;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// Can we have more than one From: address?
// TODO: indicate folder to save in (defaults to Drafts)
/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Save draft
 *     <ul>
 *       <li>Only allowed one top-level <b>&lt;mp></b> but can nest <b>&lt;mp></b>s within if
 *           multipart/* on reply/forward. Set origid on <b>&lt;m></b> element and set rt to "r" or
 *           "w", respectively.
 *       <li>Can optionally set identity-id to specify the identity being used to compose the
 *           message. If updating an existing draft, set "id" attr on <b>&lt;m></b> element.
 *       <li>Can refer to parts of existing draft in <b>&lt;attach></b> block
 *       <li>Drafts default to the Drafts folder
 *       <li>Setting folder/tags/flags/color occurs <b>after</b> the draft is created/updated, and
 *           if it fails the content <b>WILL STILL BE SAVED</b>
 *       <li>Can optionally set autoSendTime to specify the time at which the draft should be
 *           automatically sent by the server
 *       <li>The ID of the saved draft is returned in the "id" attribute of the response.
 *       <li>The ID referenced in the response's "idnt" attribute specifies the folder where the
 *           sent message is saved.
 *     </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_SAVE_DRAFT_REQUEST)
public class SaveDraftRequest {

  /**
   * @zm-api-field-tag want-imap-uid
   * @zm-api-field-description Set to return IMAP UID. (default is unset.)
   */
  @XmlAttribute(name = MailConstants.A_WANT_IMAP_UID /* wantImapUid */, required = false)
  private ZmBoolean wantImapUid;

  /**
   * @zm-api-field-tag want-modified-sequence
   * @zm-api-field-description Set to return Modified Sequence. (default is unset.)
   */
  @XmlAttribute(name = MailConstants.A_WANT_MODIFIED_SEQUENCE /* wantModSeq */, required = false)
  private ZmBoolean wantModifiedSequence;

  // E_INVITE child is not allowed
  /**
   * @zm-api-field-description Details of Draft to save
   */
  @XmlElement(name = MailConstants.E_MSG /* m */, required = false)
  private SaveDraftMsg msg;

  public SaveDraftRequest() {}

  public void setMsg(SaveDraftMsg msg) {
    this.msg = msg;
  }

  public SaveDraftMsg getMsg() {
    return msg;
  }

  public void setWantImapUid(Boolean wantImapUid) {
    this.wantImapUid = ZmBoolean.fromBool(wantImapUid);
  }

  public boolean getWantImapUid() {
    return ZmBoolean.toBool(wantImapUid, false);
  }

  public void setWantModifiedSequence(Boolean wantModSeq) {
    this.wantModifiedSequence = ZmBoolean.fromBool(wantModSeq);
  }

  public boolean getWantModifiedSequence() {
    return ZmBoolean.toBool(wantModifiedSequence, false);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("wantImapUid", wantImapUid)
        .add("wantModSeq", wantModifiedSequence)
        .add("msg", msg);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
