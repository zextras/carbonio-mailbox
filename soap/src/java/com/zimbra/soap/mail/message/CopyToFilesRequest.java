// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class that identifies a CopyToFiles request. The idea is that message id and part are
 * identifiers, so they are attributes. File description and folder id are elements of the request.
 *
 * @author davidefrison
 * @since 4.0.7
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_COPY_TO_FILES_REQUEST)
public class CopyToFilesRequest {

  /**
   * @zm-api-field-tag messageId
   * @zm-api-field-description ID of the email
   */
  @XmlAttribute(name = MailConstants.A_MESSAGE_ID /* messageId */, required = true)
  private String messageId;

  /**
   * @zm-api-field-tag part
   * @zm-api-field-description attachment part identifier
   */
  @XmlAttribute(name = MailConstants.A_PART /* part */, required = true)
  private String part;

  /**
   * @zm-api-field-tag destinationFolderId
   * @zm-api-field-description identifies folder where to upload on files
   */
  @XmlElement(
      name = MailConstants.E_DESTINATION_FOLDER_ID /* destinationFolderId */,
      required = true)
  private String destinationFolderId;

  /**
   * @zm-api-field-tag description
   * @zm-api-field-description identifies folder where to upload on files
   */
  @XmlElement(name = MailConstants.E_FILE_DESCRIPTION /* description */, required = true)
  private String description;

  public CopyToFilesRequest() {}

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getPart() {
    return part;
  }

  public void setPart(String part) {
    this.part = part;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDestinationFolderId() {
    return destinationFolderId;
  }

  public void setDestinationFolderId(String destinationFolderId) {
    this.destinationFolderId = destinationFolderId;
  }
}
