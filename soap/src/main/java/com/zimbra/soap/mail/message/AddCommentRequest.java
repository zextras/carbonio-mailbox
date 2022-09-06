// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.AddedComment;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Add a comment to the specified item. Currently comments can only be
 *     added to documents
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_ADD_COMMENT_REQUEST)
public class AddCommentRequest {

  /**
   * @zm-api-field-description Added comment
   */
  @XmlElement(name = MailConstants.E_COMMENT /* comment */, required = true)
  private final AddedComment comment;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private AddCommentRequest() {
    this((AddedComment) null);
  }

  public AddCommentRequest(AddedComment comment) {
    this.comment = comment;
  }

  public AddedComment getComment() {
    return comment;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("comment", comment);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
