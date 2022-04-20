package com.zimbra.cs.service.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.carbonio.files.FilesClient;
import com.zextras.carbonio.files.entities.NodeId;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraCookie;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/** Integration tests for CopyToFiles */
public class CopyToFilesIT {

  private FilesClient mockFilesClient;
  private AttachmentService mockAttachmentService;

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    prov.createAccount("test1@zimbra.com", "secret", new HashMap<String, Object>());
    prov.createAccount("test2@zimbra.com", "secret", new HashMap<String, Object>());
    mockFilesClient = mock(FilesClient.class);
    mockAttachmentService = mock(AttachmentService.class);
  }

  /**
   * Test response for when request is handled correctly
   *
   * @throws ServiceException
   */
  @Test
  public void shouldHandleEmailAttachment()
      throws ServiceException, IOException, MessagingException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(acct),
            acct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    // mock get upload
    MimePart mockUpload = mock(MimePart.class);
    InputStream attachmentContent =
        new ByteArrayInputStream("Hi, how, are, ye, ?".getBytes(StandardCharsets.UTF_8));
    when(mockUpload.getFileName()).thenReturn("My_file.csv");
    when(mockUpload.getContentType()).thenReturn("text/csv");
    when(mockUpload.getInputStream()).thenReturn(attachmentContent);
    when(mockAttachmentService.getAttachment(anyString(), any(), anyInt(), anyString()))
        .thenReturn(Try.success(mockUpload));
    when(mockUpload.getInputStream()).thenReturn(attachmentContent);
    CopyToFiles copyToFiles = new CopyToFiles(mockAttachmentService, mockFilesClient);
    // mock files api
    String nodeId = UUID.randomUUID().toString();
    doReturn(Try.of(() -> new NodeId(nodeId)))
        .when(mockFilesClient)
        .uploadFile(anyString(), anyString(), anyString(), anyString(), any(), anyLong());
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("1");
    up.setPart("2");
    up.setDestinationFolderId("My folder");
    Element element = JaxbUtil.jaxbToElement(up);

    // call SOAP API
    Element el = copyToFiles.handle(element, context);
    CopyToFilesResponse response = zsc.elementToJaxb(el);
    assertEquals(nodeId, response.getNodeId());
  }

  /**
   * Test SoapException for when upload file not found on mailbox.
   *
   * @throws ServiceException
   */
  @Test
  public void shouldThrowSoapFaultExceptionIfFileNotFound() throws ServiceException {
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

    try {
      copyToFiles.handle(element, context);
      fail("Did not throw SoapFault exception.");
    } catch (ServiceException serviceException) {
      assertEquals("File not found.", serviceException.getMessage());
    }
  }

  /**
   * Test exception handling for when File upload fails
   *
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
    doReturn(Try.failure(new RuntimeException("Oops, something went wrong.")))
        .when(mockFilesClient)
        .uploadFile(anyString(), anyString(), anyString(), anyString(), any(), anyLong());
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("1");
    up.setPart("2");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      copyToFiles.handle(element, context);
      fail("Did not throw SoapFault exception.");
    } catch (ServiceException serviceException) {
      assertEquals("system failure: internal error.", serviceException.getMessage());
    }
  }

  /**
   * Test case if file service returns null {@link com.zextras.carbonio.files.entities.NodeId}
   *
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
    doReturn(Try.of(() -> null))
        .when(mockFilesClient)
        .uploadFile(anyString(), anyString(), anyString(), anyString(), any(), anyLong());
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("123");
    up.setPart("Whatever you want");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      copyToFiles.handle(element, context);
      fail("Did not throw SoapFault exception.");
    } catch (ServiceException serviceException) {
      assertEquals(
          "system failure: got null response from Files server.", serviceException.getMessage());
    }
  }

  /**
   * Test call on files sdk is done right
   *
   * @throws ServiceException
   * @throws IOException
   */
  @Test
  public void shouldSendUploadedFile() throws Exception {
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
    AuthToken authToken = AuthProvider.getAuthToken(acct);
    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(
        SoapEngine.ZIMBRA_CONTEXT,
        new ZimbraSoapContext(authToken, acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12));
    // have to mock because even the Upload object has some logic in it
    MimePart mockUpload = mock(MimePart.class);
    String body = "Hi, how, are, ye, ?";
    byte[] mimeBytes = body.getBytes(StandardCharsets.UTF_8);
    InputStream uploadContent = new ByteArrayInputStream(mimeBytes);
    String fileName = "My_file.csv";
    int fileSize = mimeBytes.length;
    String contentType = "text/csv";
    // mock attachment
    when(mockUpload.getFileName()).thenReturn(fileName);
    when(mockUpload.getContentType()).thenReturn(contentType);
    when(mockUpload.getInputStream()).thenReturn(uploadContent);
    when(mockUpload.getSize()).thenReturn(fileSize);
    // mock attachment service
    when(mockAttachmentService.getAttachment(anyString(), any(), anyInt(), anyString()))
        .thenReturn(Try.success(mockUpload))
        .thenReturn(Try.success(mockUpload));
    when(mockUpload.getInputStream()).thenReturn(uploadContent);
    // mock Files client
    doReturn(Try.of(() -> null))
        .when(mockFilesClient)
        .uploadFile(anyString(), anyString(), anyString(), anyString(), any(), anyLong());
    // build request
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId("123");
    up.setPart("Whatever you want");
    up.setDestinationFolderId("FOLDER_1");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      new CopyToFiles(mockAttachmentService, mockFilesClient).handle(element, context);
    } catch (ServiceException ignored) {
    }

    verify(mockFilesClient, Mockito.times(1))
        .uploadFile(
            ZimbraCookie.COOKIE_ZM_AUTH_TOKEN + "=" + authToken.getEncoded(),
            "FOLDER_1",
            fileName,
            contentType,
            uploadContent,
            fileSize);
  }
}
