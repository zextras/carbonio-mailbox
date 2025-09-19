package com.zimbra.cs.service.servlet.preview;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class PreviewServletTest {

  private static final String REQUEST_URL_BASE = "https://mail.test.com";
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private PreviewHandler previewHandler;
  @InjectMocks
  private PreviewServlet previewServlet;
  @Mock
  private AuthToken authToken;
  private MockedStatic<ZimbraServlet> zimbraServletMockedStatic;

  @BeforeEach
  void setUp() {
    zimbraServletMockedStatic = mockStatic(ZimbraServlet.class);
    MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() {
    zimbraServletMockedStatic.close();
  }

  @Test
  void should_return_no_error_when_everything_is_ok_and_doGet_is_called() throws IOException, ServletException {
    var requestId = UUID.randomUUID().toString();

    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn(requestId);
    doNothing().when(previewHandler).handle(any(HttpServletRequest.class), any(HttpServletResponse.class));

    previewServlet.doGet(request, response);

    verify(request, never()).setAttribute(Constants.REQUEST_ID_KEY, requestId);
    verify(previewHandler).handle(request, response);
    verify(response, never()).sendError(anyInt(), anyString());
  }

  @Test
  void should_set_requestId_to_request_when_missing() throws IOException, ServletException {
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    doNothing().when(previewHandler).handle(any(HttpServletRequest.class), any(HttpServletResponse.class));

    previewServlet.doGet(request, response);

    verify(previewHandler).handle(request, response);
    verify(response, never()).sendError(anyInt(), anyString());
    verify(request, atLeastOnce()).setAttribute(eq(Constants.REQUEST_ID_KEY), anyString());
  }
}