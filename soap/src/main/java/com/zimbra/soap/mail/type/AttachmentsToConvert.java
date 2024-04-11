package com.zimbra.soap.mail.type;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

public class AttachmentsToConvert {

  @XmlElement(name= "attachmentToConvert", required=true)
  private List<AttachmentToConvert> attachmentsToConvert;

  public AttachmentsToConvert(List<AttachmentToConvert> attachmentsToConvert) {
    this.attachmentsToConvert = attachmentsToConvert;
  }

  public AttachmentsToConvert() {
  }

  public List<AttachmentToConvert> getAttachmentsToConvert() {
    return attachmentsToConvert;
  }
}
