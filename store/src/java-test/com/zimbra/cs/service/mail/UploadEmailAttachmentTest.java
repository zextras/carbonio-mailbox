package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UploadEmailAttachment.class, ZimbraSoapContext.class})
public class UploadEmailAttachmentTest {

  @Test
  public void shouldHandleEmailAttachment() throws ServiceException {
    UploadEmailAttachment uploadEmailAttachment = new UploadEmailAttachment();
    final ZimbraSoapContext mockContext = PowerMockito.mock(ZimbraSoapContext.class);
    PowerMockito.when(mockContext.createElement(MailConstants.UPLOAD_EMAIL_ATTACHMENT_RESPONSE))
        .thenReturn(new XMLElement(MailConstants.UPLOAD_EMAIL_ATTACHMENT_RESPONSE));
    Element element = new XMLElement(MailConstants.UPLOAD_EMAIL_ATTACHMENT_REQUEST);
    element.addAttribute(MailConstants.E_ATTACH, "thisIsMyFile");
    Map<String, Object> context = new HashMap<>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, mockContext);
    uploadEmailAttachment.handle(element, context);
  }

}