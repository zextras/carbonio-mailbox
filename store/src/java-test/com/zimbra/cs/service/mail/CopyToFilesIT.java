package com.zimbra.cs.service.mail;

import com.zextras.carbonio.files.FilesClient;
import com.zextras.carbonio.files.entities.NodeId;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.zmime.ZMimeBodyPart;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.internet.MimePart;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Integration tests for CopyToFiles
 */
public class CopyToFilesIT {

  private final FilesClient mockFilesClient = Mockito.mock(FilesClient.class);
  private final AttachmentService mockAttachmentService = Mockito.mock(
      AttachmentService.class);

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    prov.createAccount("test1@zimbra.com", "secret", new HashMap<String, Object>());
    prov.createAccount("test2@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
    Mockito.reset(mockFilesClient);
    Mockito.reset(mockAttachmentService);
  }

  /**
   * Test response for when request is handled correctly
   * @throws ServiceException
   */
  @Test
  public void shouldHandleEmailAttachment() throws ServiceException, IOException, MessagingException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc = new ZimbraSoapContext(AuthProvider.getAuthToken(acct),
        acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    // mock get upload
    MimePart mockUpload =  Mockito.mock(MimePart.class);
    InputStream attachmentContent = new ByteArrayInputStream("Hi, how, are, ye, ?".getBytes(StandardCharsets.UTF_8));
    Mockito.when(mockUpload.getFileName()).thenReturn("My_file.csv");
    Mockito.when(mockUpload.getContentType()).thenReturn("text/csv");
    Mockito.when(mockUpload.getInputStream()).thenReturn(attachmentContent);
    Mockito.when(
            mockAttachmentService.getAttachment(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
        .thenReturn(Try.success(mockUpload));
    Mockito.when(
            mockAttachmentService.getAttachmentRawContent(Mockito.any()))
        .thenReturn(Try.success(attachmentContent));
    CopyToFiles copyToFiles = new CopyToFiles(mockAttachmentService, mockFilesClient);
    // mock files api
    String nodeId = UUID.randomUUID().toString();
    Mockito.doReturn(Try.of(() -> new NodeId(nodeId)))
        .when(mockFilesClient).uploadFile(Mockito.anyString(),Mockito.anyString(),
            Mockito.anyString(),Mockito.anyString(),Mockito.any(), Mockito.anyLong());
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("1");
    up.setPart("2");
    up.setDestinationFolderId("My folder");
    Element element = JaxbUtil.jaxbToElement(up);

    // call SOAP API
    Element el = copyToFiles.handle(element, context);
    CopyToFilesResponse response = zsc.elementToJaxb(el);
    Assert.assertEquals(nodeId, response.getNodeId());
  }

  /**
   * Test SoapException for when upload file not found on mailbox.
   * @throws ServiceException
   */
  @Test
  public void shouldThrowSoapFaultExceptionIfFileNotFound() throws ServiceException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, new ZimbraSoapContext(AuthProvider.getAuthToken(acct),
        acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12));
    // request unknown file -> SoapFault
    CopyToFiles copyToFiles = new CopyToFiles(new MailboxAttachmentService(), mockFilesClient);
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("1");
    up.setPart("2");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      copyToFiles.handle(element, context);
      Assert.fail("Did not throw SoapFault exception.");
    } catch (SoapFaultException soapFaultException) {
      Assert.assertEquals("File not found.", soapFaultException.getMessage());
    }
  }

  /**
   * Test exception handling for when File upload fails
   * @throws ServiceException
   * @throws IOException
   */
  @Test
  public void shouldThrowSoapFaultExceptionIfFileServiceFails()
      throws ServiceException, IOException, MessagingException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, new ZimbraSoapContext(AuthProvider.getAuthToken(acct),
        acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12));
    // have to mock because even the Upload object has some logic in it
    MimePart mockAttachment =  Mockito.mock(MimePart.class);
    InputStream uploadContent = new ByteArrayInputStream("Hi, how, are, ye, ?".getBytes(StandardCharsets.UTF_8));
    Mockito.when(mockAttachment.getFileName()).thenReturn("My_file.csv");
    Mockito.when(mockAttachment.getContentType()).thenReturn("text/csv");
    Mockito.when(mockAttachment.getInputStream()).thenReturn(uploadContent);
    Mockito.when(
            mockAttachmentService.getAttachment(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
        .thenReturn(Try.success(mockAttachment));
    CopyToFiles copyToFiles = new CopyToFiles(mockAttachmentService, mockFilesClient);
    Mockito.doReturn(Try.failure(new RuntimeException("Oops, something went wrong.")))
        .when(mockFilesClient).uploadFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),
            Mockito.anyString(), Mockito.any(), Mockito.anyLong());
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("1");
    up.setPart("2");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      copyToFiles.handle(element, context);
      Assert.fail("Did not throw SoapFault exception.");
    } catch (SoapFaultException soapFaultException) {
      Assert.assertEquals("Service failure.", soapFaultException.getMessage());
    }
  }

  /**
   * Test case if file service returns null {@link com.zextras.carbonio.files.entities.NodeId}
   * @throws ServiceException
   * @throws IOException
   */
  @Test
  public void shouldThrowSoapFaultExceptionIfFileServiceReturnsNull()
      throws ServiceException, IOException, MessagingException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, new ZimbraSoapContext(AuthProvider.getAuthToken(acct),
        acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12));
    // have to mock because even the Upload object has some logic in it
    MimePart mockUpload =  Mockito.mock(MimePart.class);
    InputStream uploadContent = new ByteArrayInputStream("Hi, how, are, ye, ?".getBytes(StandardCharsets.UTF_8));
    Mockito.when(mockUpload.getFileName()).thenReturn("My_file.csv");
    Mockito.when(mockUpload.getContentType()).thenReturn("text/csv");
    Mockito.when(mockUpload.getInputStream()).thenReturn(uploadContent);
    Mockito.when(
            mockAttachmentService.getAttachment(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
        .thenReturn(Try.success(mockUpload));
    CopyToFiles copyToFiles = new CopyToFiles(mockAttachmentService, mockFilesClient);
    Mockito.doReturn(Try.of(() -> null))
        .when(mockFilesClient).uploadFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),
            Mockito.any(), Mockito.anyLong());
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("123");
    up.setPart("Whatever you want");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      copyToFiles.handle(element, context);
      Assert.fail("Did not throw SoapFault exception.");
    } catch (SoapFaultException soapFaultException) {
      Assert.assertEquals("Service failure.", soapFaultException.getMessage());
    }
  }

  /**
   * Test call on files sdk is done right
   * @throws ServiceException
   * @throws IOException
   */
  @Test
  public void shouldSendUploadedFile() throws Exception {
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
    AuthToken authToken = AuthProvider.getAuthToken(acct);
    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, new ZimbraSoapContext(authToken,
        acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12));
    // have to mock because even the Upload object has some logic in it
    MimePart mockUpload =  Mockito.mock(MimePart.class);
    String body = "Hi, how, are, ye, ?";
    byte[] mimeBytes = body.getBytes(StandardCharsets.UTF_8);
    InputStream uploadContent = new ByteArrayInputStream(mimeBytes);
    String fileName = "My_file.csv";
    int fileSize = mimeBytes.length;
    String contentType = "text/csv";
    // mock attachment
    Mockito.when(mockUpload.getFileName()).thenReturn(fileName);
    Mockito.when(mockUpload.getContentType()).thenReturn(contentType);
    Mockito.when(mockUpload.getInputStream()).thenReturn(uploadContent);
    Mockito.when(mockUpload.getSize()).thenReturn(fileSize);
    // mock attachment service
    Mockito.when(
            mockAttachmentService.getAttachment(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
        .thenReturn(Try.success(mockUpload));
    Mockito.when(mockAttachmentService.getAttachmentRawContent(mockUpload))
        .thenReturn(Try.success(uploadContent));
    // mock Files client
    Mockito.doReturn(Try.of(() -> null))
        .when(mockFilesClient).uploadFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(), Mockito.anyLong());
    // build request
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("123");
    up.setPart("Whatever you want");
    up.setDestinationFolderId("FOLDER_1");
    Element element = JaxbUtil.jaxbToElement(up);
    try {
      new CopyToFiles(mockAttachmentService, mockFilesClient).handle(element, context);
    } catch (SoapFaultException ignored) {}
    Mockito.verify(mockFilesClient, Mockito.times(1))
        .uploadFile(ZimbraCookie.COOKIE_ZM_AUTH_TOKEN + "=" + authToken.getEncoded(),
            "FOLDER_1", fileName, contentType, uploadContent, fileSize);
  }


}