// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.mail.type.DocumentActionSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Document Action
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusXmlConstants.E_DOCUMENT_ACTION_REQUEST)
public class DocumentActionRequest {

  /**
   * @zm-api-field-description Document action selector <br>
   *     Document specific operations : <b>watch|!watch|grant|!grant</b>
   */
  @XmlElement(name = MailConstants.E_ACTION /* action */, required = true)
  private DocumentActionSelector action;

  private DocumentActionRequest() {}

  private DocumentActionRequest(DocumentActionSelector action) {
    setAction(action);
  }

  public static DocumentActionRequest create(DocumentActionSelector action) {
    return new DocumentActionRequest(action);
  }

  public void setAction(DocumentActionSelector action) {
    this.action = action;
  }

  public DocumentActionSelector getAction() {
    return action;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("action", action);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
