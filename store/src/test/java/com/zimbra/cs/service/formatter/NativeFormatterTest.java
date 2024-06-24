package com.zimbra.cs.service.formatter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class NativeFormatterTest {

  @Mock
  private HttpServletRequest req;

  @Mock
  private HttpServletResponse resp;

  @Mock
  private ServletOutputStream servletOutputStream;

  @Captor
  private ArgumentCaptor<String> headerCaptor;

  @Captor
  private ArgumentCaptor<String> valueCaptor;

  @BeforeEach
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    when(resp.getOutputStream()).thenReturn(servletOutputStream);
  }

  @Test
  void should_return_original_doc_with_requested_content_disposition_when_content_is_not_scriptable()
      throws IOException {
    InputStream is = new ByteArrayInputStream("Test data".getBytes());
    var contentType = "text/plain";
    var filename = "test.txt";
    var desc = "Test description";

    when(req.getParameter("disp")).thenReturn("i");

    new NativeFormatter().sendBackOriginalDoc(is, contentType, filename, desc, req, resp);

    verify(resp, times(2)).addHeader(headerCaptor.capture(), valueCaptor.capture());

    var capturedHeaders = headerCaptor.getAllValues();
    var capturedValues = valueCaptor.getAllValues();

    var contentDispositionIndex = capturedHeaders.indexOf("Content-Disposition");
    if (contentDispositionIndex != -1) {
      assertTrue(capturedValues.get(contentDispositionIndex).contains("inline"));
    }
    verify(servletOutputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
  }

  @Test
  void should_return_original_doc_with_content_disposition_attachment_when_content_is_scriptable() throws IOException {
    var xmlContent = "<?xml version=\"1.0\"?>\n"
        + "<Tests xmlns=\"https://www.example.com\">\n"
        + "  <Test TestId=\"0001\" TestType=\"CMD\">\n"
        + "    <Name>Convert number to string</Name>\n"
        + "  </Test>\n"
        + "</Tests>";
    InputStream is = new ByteArrayInputStream(xmlContent.getBytes());
    var contentType = "text/xml";
    var filename = "test.xml";
    var desc = "Test description";

    when(req.getParameter("disp")).thenReturn("i");

    new NativeFormatter().sendBackOriginalDoc(is, contentType, filename, desc, req, resp);

    verify(resp, times(2)).addHeader(headerCaptor.capture(), valueCaptor.capture());

    var capturedHeaders = headerCaptor.getAllValues();
    var capturedValues = valueCaptor.getAllValues();

    var contentDispositionIndex = capturedHeaders.indexOf("Content-Disposition");
    if (contentDispositionIndex != -1) {
      assertTrue(capturedValues.get(contentDispositionIndex).contains("attachment"));
    }
    verify(servletOutputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
  }

}