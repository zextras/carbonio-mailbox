package com.zimbra.cs.service.mail;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import org.junit.After;
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
  private MockServerClient mockFilesServer;

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    mockFilesServer = startClientAndServer(20002);
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount(
        "shared@zimbra.com",
        "secret",
        new HashMap<String, Object>() {
          {
            put(ZAttrProvisioning.A_zimbraId, UUID.randomUUID().toString());
          }
        });
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    prov.createAccount("test1@zimbra.com", "secret", new HashMap<String, Object>());
    prov.createAccount("test2@zimbra.com", "secret", new HashMap<String, Object>());
    mockFilesClient = mock(FilesClient.class);
    mockAttachmentService = mock(AttachmentService.class);
  }

  @After
  public void tearDown() throws IOException{
    mockFilesServer.stop();
  }

  /**
   * Test: copy to files API handles return response with nodeId
   *
   * @throws ServiceException
   */
  @Test
  public void shouldHandleCopyToFilesCall()
      throws Exception {
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
    MimePart mockUpload = this.createMockUpload();
    when(mockAttachmentService.getAttachment(anyString(), any(), anyInt(), anyString()))
        .thenReturn(Try.success(mockUpload));
    CopyToFiles copyToFiles = new CopyToFiles(mockAttachmentService, mockFilesClient);
    // mock files api
    String nodeId = UUID.randomUUID().toString();
    when(mockFilesClient.uploadFile(anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
        .thenReturn(Try.of(() -> new NodeId(nodeId)));
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
   * Test: file not found on mailbox -> file not found error.
   *
   * @throws ServiceException
   */
  @Test
  public void shouldThrowServiceExceptionIfFileNotFound() throws ServiceException {
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
  public void shouldThrowServiceExceptionIfFileServiceReturnsFailure()
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
    when(mockFilesClient.uploadFile(anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
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
  public void shouldThrowServiceExceptionWhenFilesServiceReturnsNullNodeId()
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
    when(mockFilesClient.uploadFile(anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
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
   * Test: call on Files SDK is done right (parameters)
   *
   * @throws ServiceException
   * @throws IOException
   */
  @Test
  public void shouldCallFilesUploadWithCorrectParameters() throws Exception {
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
    String contentType = "text/csv";
    // mock attachment
    when(mockUpload.getFileName()).thenReturn(fileName);
    when(mockUpload.getContentType()).thenReturn(contentType);
    when(mockUpload.getInputStream()).thenReturn(uploadContent);
    // mock attachment service
    when(mockAttachmentService.getAttachment(anyString(), any(), anyInt(), anyString()))
        .thenReturn(Try.success(mockUpload))
        .thenReturn(Try.success(mockUpload));
    when(mockUpload.getInputStream()).thenReturn(uploadContent);
    // mock Files client
    when(mockFilesClient.uploadFile(anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
        .thenReturn(null);
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

    verify(mockFilesClient, times(1))
        .uploadFile(
            ZimbraCookie.COOKIE_ZM_AUTH_TOKEN + "=" + authToken.getEncoded(),
            "FOLDER_1",
            fileName,
            contentType,
            uploadContent,
            -1L);
  }

  /**
   * Test: fail to get token from context -> internal error
   *
   * @throws Exception
   */
  @Test
  public void shouldThrowServiceExceptionWithInternalFailureIfGetAuthTokenFails() throws Exception {
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

  private MimePart createMockUpload() throws Exception{
    MimePart mockUpload = mock(MimePart.class);
    when(mockUpload.getFileName()).thenReturn("My_file.csv");
    when(mockUpload.getContentType()).thenReturn("text/csv");
    when(mockUpload.getInputStream()).thenReturn(new ByteArrayInputStream("Hi, how, are, ye, ?".getBytes(StandardCharsets.UTF_8)));
    return mockUpload;
  }

  /**
   * When passing messageId as UUID:id it should use the UUID as mailbox account search scope This
   * tests CO-727 with a shared mailbox in particular.
   *
   * @throws Exception
   */
  @Test
  public void shouldUseProvidedUUIDasMailboxAccount() throws Exception {
    MimePart mockUpload = this.createMockUpload();
    final NodeId nodeId = new NodeId();
    nodeId.setNodeId("1000");
    mockFilesServer.when(
        request().withPath("/upload/")).respond(
            HttpResponse.response(new ObjectMapper().writeValueAsString(nodeId)).withStatusCode(200)
    );
    when(mockAttachmentService.getAttachment(anyString(), any(), anyInt(), anyString())).thenReturn(Try.success(mockUpload));
    // prepare request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
    String sharedAcctUUID =
        Provisioning.getInstance().get(Key.AccountBy.name, "shared@zimbra.com").getId();
    AuthToken authToken = AuthProvider.getAuthToken(acct);
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(
        SoapEngine.ZIMBRA_CONTEXT,
        new ZimbraSoapContext(authToken, acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12));
    ZimbraSoapContext zsc = mock(ZimbraSoapContext.class);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    when(zsc.getAuthToken()).thenReturn(authToken);
    CopyToFilesRequest up = new CopyToFilesRequest();
    up.setMessageId(sharedAcctUUID + ":123");
    up.setPart("2");
    up.setDestinationFolderId("FOLDER_1");
    Element element = JaxbUtil.jaxbToElement(up);
    final FilesClient filesClient = FilesClient.atURL( "http://127.0.0.1:20002");
    new CopyToFiles(mockAttachmentService, filesClient).handle(element,
        context);
    verify(mockAttachmentService, times(1)).getAttachment(sharedAcctUUID, authToken, 123, "2");
  }
}
