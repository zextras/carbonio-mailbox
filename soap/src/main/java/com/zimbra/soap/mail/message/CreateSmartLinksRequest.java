package com.zimbra.soap.mail.message;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.type.AttachmentToConvert;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="CreateSmartLinksRequest")
public class CreateSmartLinksRequest {

  @XmlAttribute(name= "messageId", required=true)
  private String messageId;

  @XmlElement(name= "attachmentToConvert", required=true)
  private List<AttachmentToConvert> attachmentsToConvert;

  public CreateSmartLinksRequest() {
  }

  public CreateSmartLinksRequest(String messageId, List<AttachmentToConvert> attachmentsToConvert) {
    this.messageId = messageId;
    this.attachmentsToConvert = attachmentsToConvert;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public List<AttachmentToConvert> getAttachmentsToConvert() {
    return attachmentsToConvert;
  }

  public void setAttachmentsToConvert(List<AttachmentToConvert> attachmentsToConvert) {
    this.attachmentsToConvert = attachmentsToConvert;
  }

  public static void main(String[] args) throws ServiceException {
    CreateSmartLinksRequest req = new CreateSmartLinksRequest("3453453-54353", List.of(
        new AttachmentToConvert("part1"),
        new AttachmentToConvert("part2")
    ));
    Element el = JaxbUtil.jaxbToElement(req);
    Element elJson = JaxbUtil.jaxbToElement(req, JSONElement.mFactory);

    System.out.println(el.toString());
    System.out.println(elJson.toString());
  }
}
