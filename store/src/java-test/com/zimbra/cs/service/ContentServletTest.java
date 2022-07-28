package com.zimbra.cs.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zimbra.common.mime.MimeConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ContentServletTest {

  @Test
  public void shouldSendBackDefangedHtmlWhenCalled() throws MessagingException, IOException {

    InputStream stubInputStream =
        IOUtils.toInputStream(
            "<a>alert(‘suspicious text MFA authentication failed. realm=%s username=%s"
                + " error_code=%s mfa_method=%s mfa_challenge_duration=%s’)</a>",
            "UTF-8");

    MimeMessage mockMimeMessage = mock(MimeMessage.class);
    when(mockMimeMessage.getInputStream()).thenReturn(stubInputStream);
    when(mockMimeMessage.getContentType()).thenReturn(MimeConstants.CT_TEXT_HTML);

    HttpServletResponse responseMock = mock(HttpServletResponse.class);
    MockServletOutputStream stubServletOutputStream = new MockServletOutputStream();
    when(responseMock.getOutputStream()).thenReturn(stubServletOutputStream);
    ContentServlet.sendbackDefangedHtml(
        mockMimeMessage,
        mockMimeMessage.getContentType(),
        responseMock,
        ContentServlet.FORMAT_DEFANGED_HTML);
    assertEquals(
        "alert(‘suspicious text MFA authentication failed. realm&#61;%s username&#61;%s"
            + " error_code&#61;%s mfa_method&#61;%s mfa_challenge_duration&#61;%s’)",
        stubServletOutputStream.output.toString());
  }

  static class MockServletOutputStream extends ServletOutputStream {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Override
    public void write(int b) throws IOException {
      output.write(b);
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener listener) {}
  }
}
