package com.zimbra.soap.mail.type;

import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class AttachmentToConvert {

  @XmlAttribute(name= MailConstants.A_PART, required=true)
  private String part;

  public AttachmentToConvert(String part) {
    this.part = part;
  }

  public AttachmentToConvert() {
  }

  public String getPart() {
    return part;
  }

  public void setPart(String part) {
    this.part = part;
  }
}
