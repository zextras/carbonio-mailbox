// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Represents a copy to files api response
 *
 * @author davidefrison
 * @since 4.0.7
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_COPY_TO_FILES_RESPONSE)
public class CopyToFilesResponse {

  /**
   * @zm-api-field-tag nodeId
   * @zm-api-field-description NodeId of files API upload response
   */
  @XmlAttribute(name = MailConstants.A_NODE_ID, required = true)
  private String nodeId;

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }
}
