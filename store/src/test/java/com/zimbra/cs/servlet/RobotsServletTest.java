package com.zimbra.cs.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RobotsServletTest {

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void doGet_should_allow_webcrawlers_when_mailKeepOutWebCrawlers_is_set_to_false() throws Exception {
    final Server localServer = Provisioning.getInstance().getLocalServer();
    localServer.setMailKeepOutWebCrawlers(false);

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    ByteArrayOutputStream outputStream = setupOutputStream(response);
    Mockito.when(request.getRequestURI()).thenReturn("/robots.txt");

    RobotsServlet servlet = new RobotsServlet();
    servlet.doGet(request, response);
    String servletResponse = outputStream.toString();

    assertEquals("User-agent: *\r\n" + "Allow: /\r\n", servletResponse);
  }

  @Test
  void doGet_should_disallow_webcrawlers_when_mailKeepOutWebCrawlers_is_set_to_true() throws Exception {
    final Server localServer = Provisioning.getInstance().getLocalServer();
    localServer.setMailKeepOutWebCrawlers(true);

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    ByteArrayOutputStream outputStream = setupOutputStream(response);
    Mockito.when(request.getRequestURI()).thenReturn("/robots.txt");

    RobotsServlet servlet = new RobotsServlet();
    servlet.doGet(request, response);
    String servletResponse = outputStream.toString();

    assertEquals("User-agent: *\r\n" + "Disallow: /\r\n", servletResponse);
  }

  private ByteArrayOutputStream setupOutputStream(HttpServletResponse response) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ServletOutputStream servletOutputStream = new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
      }

      @Override
      public void write(int b) {
        outputStream.write(b);
      }
    };

    Mockito.when(response.getOutputStream()).thenReturn(servletOutputStream);

    return outputStream;
  }
}