package com.zimbra.cs.service.mail;

import com.zextras.carbonio.files.FilesClient;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.service.MailboxAttachmentService;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import org.junit.Assert;
import org.junit.Test;

public class CopyToFilesTest {

  @Test
  public void shouldMapElementToCopyToFilesRequest() {
    Element element = new XMLElement(MailConstants.COPY_TO_DRIVE_REQUEST);
    String mailId = "380";
    String attachmentPart = "2";
    element.addUniqueElement(new XMLElement(MailConstants.A_MESSAGE_ID)).setText(mailId);
    element.addUniqueElement(new XMLElement(MailConstants.A_PART)).setText(attachmentPart);
    CopyToFiles copyToFiles =  new CopyToFiles(new MailboxAttachmentService(), FilesClient.atURL(""));
    CopyToFilesRequest copyToFilesRequest = copyToFiles.getRequestObject(element).get();
    Assert.assertEquals(mailId, copyToFilesRequest.getMessageId());
    Assert.assertEquals(attachmentPart, copyToFilesRequest.getPart());
  }

}
