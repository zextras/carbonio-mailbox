package com.zimbra.cs.service.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.FilesClient;
import com.zextras.carbonio.files.entities.NodeId;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import com.zimbra.cs.service.MailboxAttachmentService;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CopyToDriveRequest;
import com.zimbra.soap.mail.message.CopyToDriveResponse;
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
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class CopyToDriveTest {

  private final FilesClient mockFilesClient = Mockito.mock(FilesClient.class);
  private final AttachmentService mockAttachmentService = Mockito.mock(
      AttachmentService.class);
  public static MockWebServer mockBackEnd;

  @BeforeClass
  public static void startServer() throws IOException {
    mockBackEnd = new MockWebServer();
    mockBackEnd.start();
  }

  @AfterClass
  public static void stopSever() throws IOException {
    mockBackEnd.shutdown();
  }

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
    InputStream uploadContent = new ByteArrayInputStream("Hi, how, are, ye, ?".getBytes(StandardCharsets.UTF_8));
    Mockito.when(mockUpload.getFileName()).thenReturn("My_file.csv");
    Mockito.when(mockUpload.getContentType()).thenReturn("text/csv");
    Mockito.when(mockUpload.getInputStream()).thenReturn(uploadContent);
    Mockito.when(
            mockAttachmentService.getAttachment(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
        .thenReturn(Try.success(mockUpload));
    CopyToDrive copyToDrive = new CopyToDrive(mockAttachmentService, mockFilesClient);
    // mock files api
    String nodeId = UUID.randomUUID().toString();
    Mockito.doReturn(Try.of(() -> new NodeId(nodeId)))
        .when(mockFilesClient).uploadFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any());
    CopyToDriveRequest up = new CopyToDriveRequest();
    up.setMessageId("1");
    up.setPart("2");
    Element element = JaxbUtil.jaxbToElement(up);

    // call SOAP API
    Element el = copyToDrive.handle(element, context);
    CopyToDriveResponse response = zsc.elementToJaxb(el);
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
    CopyToDrive copyToDrive = new CopyToDrive(new MailboxAttachmentService(), mockFilesClient);
    CopyToDriveRequest up = new CopyToDriveRequest();
    up.setMessageId("1");
    up.setPart("2");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      copyToDrive.handle(element, context);
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
    CopyToDrive copyToDrive = new CopyToDrive(mockAttachmentService, mockFilesClient);
    Mockito.doReturn(Try.failure(new RuntimeException("Oops, something went wrong.")))
        .when(mockFilesClient).uploadFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any());
    CopyToDriveRequest up = new CopyToDriveRequest();
    up.setMessageId("1");
    up.setPart("2");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      copyToDrive.handle(element, context);
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
    CopyToDrive copyToDrive = new CopyToDrive(mockAttachmentService, mockFilesClient);
    Mockito.doReturn(Try.of(() -> null))
        .when(mockFilesClient).uploadFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any());
    CopyToDriveRequest up = new CopyToDriveRequest();
    up.setMessageId("123");
    up.setPart("Whatever you want");
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      copyToDrive.handle(element, context);
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
  public void shouldSendUploadedFile() throws Exception {
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, new ZimbraSoapContext(AuthProvider.getAuthToken(acct),
        acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12));
    // have to mock because even the Upload object has some logic in it
    MimePart mockUpload =  Mockito.mock(MimePart.class);
    String body = "Hi, how, are, ye, ?";
    InputStream uploadContent = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
    Mockito.when(mockUpload.getFileName()).thenReturn("My_file.csv");
    Mockito.when(mockUpload.getContentType()).thenReturn("text/csv");
    Mockito.when(mockUpload.getInputStream()).thenReturn(uploadContent);
    Mockito.when(
            mockAttachmentService.getAttachment(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
        .thenReturn(Try.success(mockUpload));
    CopyToDrive copyToDrive = new CopyToDrive(mockAttachmentService,
        FilesClient.atURL(String.format("http://%s:%d", mockBackEnd.getHostName(), mockBackEnd.getPort())));
    mockBackEnd.enqueue(new MockResponse().setResponseCode(200).setBody(new ObjectMapper().writeValueAsString(new NodeId()))
        .addHeader("Content-Type", "application/json"));
    Mockito.doReturn(Try.of(() -> null))
        .when(mockFilesClient).uploadFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any());
    CopyToDriveRequest up = new CopyToDriveRequest();
    up.setMessageId("123");
    up.setPart("Whatever you want");
    Element element = JaxbUtil.jaxbToElement(up);
    copyToDrive.handle(element, context);
    RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    String receivedBody = IOUtils.toString(recordedRequest.getBody().inputStream().readAllBytes());
    Assert.assertEquals(receivedBody,body);
  }


}