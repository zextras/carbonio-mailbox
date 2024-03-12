package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class AttachmentToConvert {

  @XmlAttribute(name = "partName", required = true)
  private String partName;
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

