package com.zimbra.cs.service.mail;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.FilesClient;
import com.zextras.carbonio.files.entities.NodeId;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.MailboxAttachmentService;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpResponse;

/** Integration tests for CopyToFiles */
public class CopyToFilesIT {

  private FilesClient mockFilesClient;
  private AttachmentService mockAttachmentService;
  private MockServerClient filesServer;

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    filesServer = startClientAndServer(20002);
    Provisioning prov = Provisioning.getInstance();
    final Account sharedAcct =
        prov.createAccount(
            "shared@zimbra.com",
            "secret",
            new HashMap<String, Object>() {
              {
                put(ZAttrProvisioning.A_zimbraId, UUID.randomUUID().toString());
              }
            });
    final Account delegated =
        prov.createAccount("delegated@zimbra.com", "secret", new HashMap<String, Object>());
    // Grant sendAs to delegated@
    final Set<ZimbraACE> aces =
        new HashSet<ZimbraACE>() {
          {
            add(
                new ZimbraACE(
                    delegated.getId(),
                    GranteeType.GT_USER,
                    RightManager.getInstance().getRight(Right.RT_sendAs),
                    RightModifier.RM_CAN_DELEGATE,
                    null));
          }
        };
    ACLUtil.grantRight(Provisioning.getInstance(), sharedAcct, aces);
    // Grant shared@ root folder access to delegated@
    final short rwidx = ACL.stringToRights("rwidx");
    final Mailbox sharedAcctMailbox = MailboxManager.getInstance().getMailboxByAccount(sharedAcct);
    final Folder rootSharedAcctFolder = sharedAcctMailbox.getFolderByPath(null, "/");
    sharedAcctMailbox.grantAccess(
        null,
        rootSharedAcctFolder.getFolderId(),
        delegated.getId(),
        ACL.GRANTEE_AUTHUSER,
        rwidx,
        null);
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    prov.createAccount("test1@zimbra.com", "secret", new HashMap<String, Object>());
    mockFilesClient = mock(FilesClient.class);
    mockAttachmentService = mock(AttachmentService.class);
  }

  @After
  public void tearDown() throws IOException {
    filesServer.stop();
  }

  /**
   * Test: copy to files API handles return response with nodeId Creates a Draft with attachment,
   * calls Files with attachment
   *
   * @throws ServiceException
   */
  @Test
  public void shouldReturnNodeIdWhenUploadingAttachment() throws Exception {
    final String email = "test@zimbra.com";
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, email);
    final Message message = this.createDraftWithFileAttachment(email);
    final NodeId nodeId = new NodeId();
    nodeId.setNodeId("1000");
    filesServer
        .when(request().withPath("/upload/"))
        .respond(
            HttpResponse.response(new ObjectMapper().writeValueAsString(nodeId))
                .withStatusCode(200));
    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(acct),
            acct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    CopyToFiles copyToFiles =
        new CopyToFiles(
            new MailboxAttachmentService(), FilesClient.atURL("http://127.0.0.1:20002"));
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId(String.valueOf(message.getId()));
    up.setPart("1");
    up.setDestinationFolderId("My folder");
    Element element = JaxbUtil.jaxbToElement(up);
    // call SOAP API
    Element el = copyToFiles.handle(element, context);
    CopyToFilesResponse response = zsc.elementToJaxb(el);
    // return should be equal to Files response
    assertEquals(nodeId.getNodeId(), response.getNodeId());
  }

  /**
   * Test passing messageId as UUID:id. This happens in case of delegation/shared mailbox.
   *
   * @throws Exception
   */
  @Test
  public void shouldReturnNodeIdWhenUploadingSharedMailboxAttachment() throws Exception {
    final NodeId nodeId = new NodeId();
    nodeId.setNodeId("1000");
    filesServer
        .when(request().withPath("/upload/"))
        .respond(
            HttpResponse.response(new ObjectMapper().writeValueAsString(nodeId))
                .withStatusCode(200));
    final String sharedEmail = "shared@zimbra.com";
    final Account delegatedAcct =
        Provisioning.getInstance().get(Key.AccountBy.name, "delegated@zimbra.com");
    final Message draftWithFileAttachment = this.createDraftWithFileAttachment(sharedEmail);
    // prepare request
    String sharedAcctUUID = Provisioning.getInstance().get(Key.AccountBy.name, sharedEmail).getId();
    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(delegatedAcct),
            delegatedAcct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    CopyToFiles copyToFiles =
        new CopyToFiles(
            new MailboxAttachmentService(), FilesClient.atURL("http://127.0.0.1:20002"));
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId(sharedAcctUUID + ":" + draftWithFileAttachment.getId());
    up.setPart("1");
    up.setDestinationFolderId("FOLDER_1");
    Element element = JaxbUtil.jaxbToElement(up);
    Element el = copyToFiles.handle(element, context);
    final CopyToFilesResponse response = zsc.elementToJaxb(el);
    Assert.assertEquals(nodeId.getNodeId(), response.getNodeId());
  }

  /**
   * Test: file not found on mailbox -> file not found error.
   *
   * @throws ServiceException
   */
  @Test
  public void shouldThrowServiceExceptionWhenFileNotFound() throws ServiceException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(
        SoapEngine.ZIMBRA_CONTEXT,
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(acct),
            acct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12));
    // request unknown file -> SoapFault
    CopyToFiles copyToFiles = new CopyToFiles(new MailboxAttachmentService(), mockFilesClient);
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("1");
    up.setPart("2");
    Element element = JaxbUtil.jaxbToElement(up);
    exceptionRule.expect(ServiceException.class);
    exceptionRule.expectMessage("File not found.");
    copyToFiles.handle(element, context);
  }

  /**
   * Test: Files SDK exception -> internal error
   *
   * @throws ServiceException
   * @throws IOException
   */
  @Test
  public void shouldThrowServiceExceptionWhenFileServiceReturnsFailure()
      throws ServiceException, IOException, MessagingException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(
        SoapEngine.ZIMBRA_CONTEXT,
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(acct),
            acct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12));
    // have to mock because even the Upload object has some logic in it
    MimePart mockAttachment = mock(MimePart.class);
    InputStream uploadContent =
        new ByteArrayInputStream("Hi, how, are, ye, ?".getBytes(StandardCharsets.UTF_8));
    when(mockAttachment.getFileName()).thenReturn("My_file.csv");
    when(mockAttachment.getContentType()).thenReturn("text/csv");
    when(mockAttachment.getInputStream()).thenReturn(uploadContent);
    when(mockAttachmentService.getAttachment(anyString(), any(), anyInt(), anyString()))
        .thenReturn(Try.success(mockAttachment));
    CopyToFiles copyToFiles = new CopyToFiles(mockAttachmentService, mockFilesClient);
    when(mockFilesClient.uploadFile(
            anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
        .thenThrow(new RuntimeException("Oops, something went wrong."));
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("1");
    up.setPart("2");
    Element element = JaxbUtil.jaxbToElement(up);
    exceptionRule.expect(ServiceException.class);
    exceptionRule.expectMessage("system failure: internal error.");
    copyToFiles.handle(element, context);
  }

  /**
   * Test: if Files SDK returns null -> error message
   *
   * @throws ServiceException
   * @throws IOException
   */
  @Test
  public void shouldThrowServiceExceptionWhenFilesReturnsNullNodeId()
      throws ServiceException, IOException, MessagingException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(
        SoapEngine.ZIMBRA_CONTEXT,
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(acct),
            acct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12));
    // have to mock because even the Upload object has some logic in it
    MimePart mockUpload = mock(MimePart.class);
    InputStream uploadContent =
        new ByteArrayInputStream("Hi, how, are, ye, ?".getBytes(StandardCharsets.UTF_8));
    when(mockUpload.getFileName()).thenReturn("My_file.csv");
    when(mockUpload.getContentType()).thenReturn("text/csv");
    when(mockUpload.getInputStream()).thenReturn(uploadContent);
    when(mockAttachmentService.getAttachment(anyString(), any(), anyInt(), anyString()))
        .thenReturn(Try.success(mockUpload));
    CopyToFiles copyToFiles = new CopyToFiles(mockAttachmentService, mockFilesClient);
    when(mockFilesClient.uploadFile(
            anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
        .thenReturn(Try.of(() -> null));
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("123");
    up.setPart("Whatever you want");
    Element element = JaxbUtil.jaxbToElement(up);
    exceptionRule.expect(ServiceException.class);
    exceptionRule.expectMessage("system failure: got null response from Files server.");
    copyToFiles.handle(element, context);
  }

  /**
   * Test: fail to get token from context -> internal error
   *
   * @throws Exception
   */
  @Test
  public void shouldThrowWithInternalErrorWhenGetAuthTokenFails() throws Exception {
    // prepare request
    ZimbraSoapContext zsc = mock(ZimbraSoapContext.class);
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    when(zsc.getAuthToken()).thenThrow(RuntimeException.class);
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("123");
    up.setPart("Whatever you want");
    up.setDestinationFolderId("FOLDER_1");
    Element element = JaxbUtil.jaxbToElement(up);
    exceptionRule.expect(ServiceException.class);
    exceptionRule.expectMessage("system failure: internal error.");
    new CopyToFiles(mockAttachmentService, mockFilesClient).handle(element, context);
  }

  /**
   * Creates a draft for Save to Files
   *
   * @param sender
   * @throws Exception
   */
  private Message createDraftWithFileAttachment(String sender) throws Exception {
    final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, sender);
    final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    final OperationContext operationContext = new OperationContext(acct);
    Address[] recipients = new Address[] {new InternetAddress(acct.getName())};
    mimeMessage.setFrom(new InternetAddress(acct.getName()));
    mimeMessage.setRecipients(RecipientType.TO, recipients);
    mimeMessage.setSubject("Test email");
    Multipart multipart = new MimeMultipart();
    MimeBodyPart attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile(
        new File(this.getClass().getResource("/test-save-to-files.txt").getFile()));
    multipart.addBodyPart(attachmentPart);
    mimeMessage.setContent(multipart);
    mimeMessage.setSender(new InternetAddress(acct.getName()));
    final ParsedMessage parsedMessage =
        new ParsedMessage(mimeMessage, mailbox.attachmentsIndexingEnabled());
    return mailbox.saveDraft(operationContext, parsedMessage, Mailbox.ID_AUTO_INCREMENT);
  }
}
