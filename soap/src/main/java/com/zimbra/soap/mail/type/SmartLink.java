// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail.type;

import com.zimbra.common.soap.SmartLinkConstants;
import javax.xml.bind.annotation.XmlAttribute;

public class SmartLink {

  /**
   * @zm-api-field-tag partName
   * @zm-api-field-description part name of attachment to convert to smartlink
   */
  @XmlAttribute(name = SmartLinkConstants.A_PART_NAME /* sfd */, required = true)
  private String partName;
  /**
   * @zm-api-field-tag draftId
   * @zm-api-field-description draftId of the message the attachment belongs to
   */
  @XmlAttribute(name = SmartLinkConstants.A_DRAFT_ID /* sfd */, required = true)
  private String draftId;

  public SmartLink() {
  }

  public SmartLink(String partName, String draftId) {
    this.partName = partName;
    this.draftId = draftId;
  }

  public String getPartName() {
    return partName;
  }

  public String getDraftId() {
    return draftId;
  }
}
