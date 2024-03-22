package com.zimbra.soap.mail.message;

import com.zimbra.soap.mail.type.AttachmentToConvert;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Create smart links for attachments
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "CreateSmartLinksRequest")
public class CreateSmartLinksRequest {

  /**
   * @zm-api-field-tag attachments
   * @zm-api-field-description attachments to convert to smartlinks
   */
  @XmlElement(name = "attachments", required = true)
  private List<AttachmentToConvert> attachments;

  public CreateSmartLinksRequest(List<AttachmentToConvert> attachments) {
    this.attachments = attachments;
  }

  public CreateSmartLinksRequest() {
  }

  public List<AttachmentToConvert> getAttachments() {
    return attachments;
  }

}
