package com.zimbra.cs.service.mail;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.UploadAttachmentRequest;
import com.zimbra.soap.mail.message.UploadAttachmentResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class UploadEmailAttachmentTest {

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
  }

  @Test
  public void shouldHandleEmailAttachment() throws ServiceException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc = new ZimbraSoapContext(AuthProvider.getAuthToken(acct),
        acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    // mock file retrieval
    //TODO: decide a behavior for this mock
    Upload mockUpload = Mockito.mock(FileUploadServlet.Upload.class);
    UploadEmailAttachment uploadEmailAttachment = new UploadEmailAttachment((a,b,c) ->
        Mockito.mock(FileUploadServlet.Upload.class));
    UploadAttachmentRequest up = new UploadAttachmentRequest();
    up.setUploadId(UUID.randomUUID().toString());
    Element element = JaxbUtil.jaxbToElement(up);

    // call SOAP API
    Element el = uploadEmailAttachment.handle(element, context);
    UploadAttachmentResponse response = zsc.elementToJaxb(el);
    Assert.assertNotNull(response);
  }

  @Test
  public void shouldThrowSoapFaultExceptionIfFileNotFound() throws ServiceException {
    // get account that will do the SOAP request
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    // prepare request
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, new ZimbraSoapContext(AuthProvider.getAuthToken(acct),
        acct.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12));
    // request unknown file -> SoapFault
    UploadEmailAttachment uploadEmailAttachment = new UploadEmailAttachment(FileUploadServlet::fetchUpload);
    UploadAttachmentRequest up = new UploadAttachmentRequest();
    up.setUploadId(UUID.randomUUID().toString());
    Element element = JaxbUtil.jaxbToElement(up);

    try {
      uploadEmailAttachment.handle(element, context);
      Assert.fail("UploadEmailAttachment did not throw SoapFault exception.");
    } catch (SoapFaultException ignore) {}

  }

}