package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class AttachmentToConvert {

  /**
   * @zm-api-field-tag partName
   * @zm-api-field-description part name of the attachment
   */
  @XmlAttribute(name = "partName", required = true)
  private String partName;

  /**
   * @zm-api-field-tag draftId
   * @zm-api-field-description id of the draft mail the attachment belongs to
   */
  @XmlAttribute(name = "draftId", required = true)
  private String draftId;

  public AttachmentToConvert(String draftId, String partName) {
    this.draftId = draftId;
    this.partName = partName;
  }

  public AttachmentToConvert() {
  }

  public String getDraftId() {
    return draftId;
  }

  public String getPartName() {
    return partName;
  }
}

