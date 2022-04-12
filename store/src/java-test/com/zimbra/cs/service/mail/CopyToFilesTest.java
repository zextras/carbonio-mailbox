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
    String description = "This is a description.";
    String destinationFolderId = "10";
    element.addUniqueElement(new XMLElement(MailConstants.A_MESSAGE_ID)).setText(mailId);
    element.addUniqueElement(new XMLElement(MailConstants.A_PART)).setText(attachmentPart);
    element.addUniqueElement(new XMLElement(MailConstants.A_DESCRIPTION)).setText(description);
    element.addUniqueElement(new XMLElement(MailConstants.A_DESTINATION_FOLDER_ID)).setText(destinationFolderId);
    CopyToFiles copyToFiles =  new CopyToFiles(new MailboxAttachmentService(), FilesClient.atURL(""));
    CopyToFilesRequest copyToFilesRequest = copyToFiles.getRequestObject(element).get();
    Assert.assertEquals(mailId, copyToFilesRequest.getMessageId());
    Assert.assertEquals(attachmentPart, copyToFilesRequest.getPart());
    Assert.assertEquals(description, copyToFilesRequest.getDescription());
    Assert.assertEquals(destinationFolderId, copyToFilesRequest.getDestinationFolderId());
  }

  @Test
  public void shouldReturnDestinationFolderIfValuePresent() {
    CopyToFilesRequest copyToFilesRequest = new CopyToFilesRequest();
    String mailId = "380";
    String attachmentPart = "2";
    String expectedFolderId = "10";
    copyToFilesRequest.setDestinationFolderId(expectedFolderId);
    copyToFilesRequest.setMessageId(mailId);
    copyToFilesRequest.setPart(attachmentPart);
    String destFolderId = new CopyToFiles(new MailboxAttachmentService(), FilesClient.atURL(""))
          .getDestinationFolderId(copyToFilesRequest).get();
    Assert.assertEquals(expectedFolderId, destFolderId);
  }

  @Test
  public void shouldReturnDefaultDestinationFolderIfValueMissing() {
    CopyToFilesRequest copyToFilesRequest = new CopyToFilesRequest();
    String mailId = "380";
    String attachmentPart = "2";
    copyToFilesRequest.setMessageId(mailId);
    copyToFilesRequest.setPart(attachmentPart);
    String destFolderId = new CopyToFiles(new MailboxAttachmentService(), FilesClient.atURL(""))
        .getDestinationFolderId(copyToFilesRequest).get();
    Assert.assertEquals("LOCAL_ROOT", destFolderId);
  }

  @Test
  public void shouldReturnDefaultDestinationFolderIfValueEmptyString() {
    CopyToFilesRequest copyToFilesRequest = new CopyToFilesRequest();
    String mailId = "380";
    String attachmentPart = "2";
    copyToFilesRequest.setMessageId(mailId);
    copyToFilesRequest.setDestinationFolderId("");
    copyToFilesRequest.setPart(attachmentPart);
    String destFolderId = new CopyToFiles(new MailboxAttachmentService(), FilesClient.atURL(""))
        .getDestinationFolderId(copyToFilesRequest).get();
    Assert.assertEquals("LOCAL_ROOT", destFolderId);
  }

}
