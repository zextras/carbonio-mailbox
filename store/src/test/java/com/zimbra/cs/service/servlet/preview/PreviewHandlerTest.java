package com.zimbra.cs.service.servlet.preview;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.exceptions.InternalServerError;
import com.zextras.carbonio.preview.exceptions.ItemNotFound;
import com.zextras.carbonio.preview.queries.Query;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.servlet.ZimbraServlet;
import io.vavr.control.Try;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import javax.mail.MessagingException;
import javax.mail.internet.MimePart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


class PreviewHandlerTest {

  private static final String REQUEST_URL_BASE = "https://mail.test.com";
  @Mock
  private PreviewClient previewClient;
  @Mock
  private AttachmentService attachmentService;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private AuthToken authToken;
  @Mock
  private ItemId itemId;
  @Mock
  private MimePart mimePart;
  @Mock
  private ItemIdFactory itemIdFactory;
  @Mock
  private com.zextras.carbonio.preview.queries.BlobResponse blobResponse;
  @InjectMocks
  private PreviewHandler previewHandler;

  private MockedStatic<ZimbraServlet> zimbraServletMockedStatic;

  private static Stream<Arguments> pathAndQueryParamsVariants() {
    return Stream.of(
        Arguments.of("27310", "service/preview/image/27310/2/0x0/?quality=high"),
        Arguments.of("27310", "service/preview/pdf/27310/2/0x0/?quality=high"),
        Arguments.of("27310", "service/preview/document/27310/2/0x0/?quality=high"),
        Arguments.of("27310", "service/preview/image/27310/2/0x0/thumbnail/?quality=high"),
        Arguments.of("27310", "service/preview/pdf/27310/2/0x0/thumbnail/?quality=high"),
        Arguments.of("27310", "service/preview/document/27310/2/0x0/thumbnail/?quality=high"),
        Arguments.of("27310", "service/preview/image/27310/2/0x0/thumbnail?quality=high"),
        Arguments.of("27310", "service/preview/pdf/27310/2/0x0/thumbnail?quality=high"),
        Arguments.of("27310", "service/preview/document/27310/2/0x0/thumbnail?quality=high"),
        Arguments.of("27310", "service/preview/document/accountId:27310/2/0x0/thumbnail?quality=high")
    );
  }

  @BeforeEach
  public void setUp() {
    zimbraServletMockedStatic = mockStatic(ZimbraServlet.class);
    MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() {
    zimbraServletMockedStatic.close();
  }

  @Test
  void should_return_error_when_preview_service_is_down() throws IOException {
    when(previewClient.healthReady()).thenReturn(false);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");

    previewHandler.handle(request, response);

    verify(response).sendError(Constants.STATUS_UNPROCESSABLE_ENTITY,
        "Preview service is down or not available to take request");
  }

  @Test
  void should_return_error_when_missing_auth_token() throws IOException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");

    previewHandler.handle(request, response);

    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "Authentication required. Request missing authentication token.");
  }

  @Test
  void should_return_error_when_missing_required_parameters() throws IOException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/"); // misses: 27310/2/0x0/?quality=high
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);

    previewHandler.handle(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
  }

  @Test
  void should_return_error_when_missing_message_id_is_invalid() throws IOException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/27310_invalid/2/0x0/?quality=high");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);

    previewHandler.handle(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
  }

  @ParameterizedTest
  @MethodSource("pathAndQueryParamsVariants")
  void should_call_getAttachment_with_supported_params(String itemIdStr, String queryString)
      throws IOException, ServiceException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(authToken.getAccountId()).thenReturn("accountId");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn(queryString);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(Integer.parseInt(itemIdStr));
    when(itemId.isLocal()).thenReturn(true);
    when(itemIdFactory.create(itemIdStr, "accountId")).thenReturn(itemId);
    when(attachmentService.getAttachmentByItemId(itemId.getAccountId(), authToken, itemId, "2"))
        .thenReturn(Try.success(mimePart));

    previewHandler.handle(request, response);

    verify(attachmentService).getAttachmentByItemId(itemId.getAccountId(), authToken, itemId, "2");
  }

  @ParameterizedTest
  @MethodSource("pathAndQueryParamsVariants")
  void should_call_getAttachmentPreview_with_supported_params(String itemIdStr, String queryString)
      throws IOException, ServiceException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(authToken.getAccountId()).thenReturn("accountId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn(queryString);
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(Integer.parseInt(itemIdStr));
    when(itemId.isLocal()).thenReturn(true);
    when(itemIdFactory.create(itemIdStr, "accountId")).thenReturn(itemId);
    when(attachmentService.getAttachmentByItemId(itemId.getAccountId(), authToken, itemId, "2"))
        .thenReturn(Try.success(mimePart));

    var previewHandlerSpy = spy(previewHandler);
    previewHandlerSpy.handle(request, response);

    verify(previewHandlerSpy).getAttachmentPreview(any(HttpServletRequest.class), any(DataBlob.class));
  }

  @Test
  void should_return_error_when_fails_to_create_item_id() throws IOException, ServiceException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/27310/2/0x0/?quality=high");
    when(authToken.getAccountId()).thenReturn("accountId");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemIdFactory.create("27310", "accountId")).thenThrow(ServiceException.class);

    previewHandler.handle(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,
        "Error processing requested attachment. Ensure message ID or account are correct.");
  }

  @Test
  void should_return_error_when_unsupported_attachments() throws IOException, ServiceException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/unsupported/27310/2/0x0/?quality=high");
    when(authToken.getAccountId()).thenReturn("accountId");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(27310);
    when(itemId.isLocal()).thenReturn(true);
    when(itemIdFactory.create("27310", "accountId")).thenReturn(itemId);
    when(attachmentService.getAttachmentByItemId(itemId.getAccountId(), authToken, itemId, "2"))
        .thenReturn(Try.success(mimePart));

    previewHandler.handle(request, response);

    verify(response).sendError(Constants.STATUS_UNPROCESSABLE_ENTITY,
        "invalid request: [requestId] Cannot handle request");
  }

  @Test
  void should_proxy_request_when_attachment_is_not_local() throws IOException, ServiceException, HttpException {
    when(previewClient.healthReady()).thenReturn(true);
    when(authToken.getAccountId()).thenReturn("accountId");
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/27310/2/0x0/?quality=high");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(27310);
    when(itemId.isLocal()).thenReturn(false);
    when(itemIdFactory.create("27310", "accountId")).thenReturn(itemId);
    when(attachmentService.getAttachment(itemId.getAccountId(), authToken, itemId.getId(), "2"))
        .thenReturn(Try.success(mimePart));

    var previewHandlerSpy = spy(previewHandler);
    previewHandlerSpy.handle(request, response);

    verify(previewHandlerSpy, times(1)).proxyRequestToTargetMailHost(request, response, itemId.getAccountId());
  }

  @Test
  void should_return_error_if_not_able_to_get_attachment_from_attachment_service()
      throws ServiceException, IOException {
    when(previewClient.healthReady()).thenReturn(true);
    when(authToken.getAccountId()).thenReturn("accountId");
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/27310/2/0x0/?quality=high");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(27310);
    when(itemId.isLocal()).thenReturn(true);
    when(itemIdFactory.create("27310", "accountId")).thenReturn(itemId);
    when(attachmentService.getAttachmentByItemId(itemId.getAccountId(), authToken, itemId, "2"))
        .thenReturn(Try.failure(new ItemNotFound()));

    previewHandler.handle(request, response);

    verify(response).sendError(Constants.STATUS_UNPROCESSABLE_ENTITY,
        "Something went wrong while accessing attachment.");
  }

  @Test
  void should_return_error_if_not_able_to_get_preview_from_preview_service() throws ServiceException, IOException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/27310/2/0x0/?quality=high");
    when(authToken.getAccountId()).thenReturn("accountId");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(27310);
    when(itemId.isLocal()).thenReturn(true);
    when(itemIdFactory.create("27310", "accountId")).thenReturn(itemId);
    when(attachmentService.getAttachmentByItemId(itemId.getAccountId(), authToken, itemId, "2"))
        .thenReturn(Try.success(mimePart));
    when(previewClient.postPreviewOfImage(any(InputStream.class), any(Query.class), any(String.class)))
        .thenReturn(Try.failure(new InternalServerError()));

    previewHandler.handle(request, response);

    verify(response).sendError(Constants.STATUS_UNPROCESSABLE_ENTITY,
        "Something went wrong while processing preview of attachment.");
  }

  @Test
  void should_respond_with_success_when_everything_ok() throws IOException, ServiceException, MessagingException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/27310/2/0x0/?quality=high");
    when(authToken.getAccountId()).thenReturn("accountId");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(27310);
    when(itemId.isLocal()).thenReturn(true);
    when(itemIdFactory.create("27310", "accountId")).thenReturn(itemId);
    when(mimePart.getFileName()).thenReturn("filename");
    when(mimePart.getInputStream()).thenReturn(Mockito.mock(InputStream.class));
    when(mimePart.getContentType()).thenReturn("text/plain");
    when(mimePart.getSize()).thenReturn(200);
    when(attachmentService.getAttachmentByItemId(itemId.getAccountId(), authToken, itemId, "2"))
        .thenReturn(Try.success(mimePart));
    when(previewClient.postPreviewOfImage(any(InputStream.class), any(Query.class), anyString())).thenReturn(
        Try.success(blobResponse));

    var previewHandlerSpy = spy(previewHandler);
    previewHandlerSpy.handle(request, response);

    verify(previewHandlerSpy).respondWithSuccess(eq(response), eq(request), any(DataBlob.class));
  }

  @Test
  void should_map_server_errors_to_unprocessable_entity_error()
      throws IOException, ServiceException, MessagingException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/27310/2/0x0/?quality=high");
    when(authToken.getAccountId()).thenReturn("accountId");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(27310);
    when(itemId.isLocal()).thenReturn(true);
    when(itemIdFactory.create("27310", "accountId")).thenReturn(itemId);
    when(mimePart.getFileName()).thenReturn("filename");
    when(mimePart.getInputStream()).thenReturn(Mockito.mock(InputStream.class));
    when(mimePart.getContentType()).thenReturn("text/plain");
    when(mimePart.getSize()).thenReturn(200);
    when(attachmentService.getAttachmentByItemId(itemId.getAccountId(), authToken, itemId, "2"))
        .thenReturn(Try.success(mimePart));
    when(previewClient.postPreviewOfImage(any(InputStream.class), any(Query.class), anyString())).thenReturn(
        Try.failure(new InternalServerError()));

    previewHandler.handle(request, response);

    verify(response).sendError(Constants.STATUS_UNPROCESSABLE_ENTITY,
        "Something went wrong while processing preview of attachment.");
  }

  @Test
  void should_map_account_errors_to_custom_errors()
      throws IOException, ServiceException {
    when(previewClient.healthReady()).thenReturn(true);
    when(request.getAttribute(Constants.REQUEST_ID_KEY)).thenReturn("requestId");
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL_BASE));
    when(request.getQueryString()).thenReturn("service/preview/image/27310/2/0x0/?quality=high");
    when(authToken.getAccountId()).thenReturn("accountId");
    when(ZimbraServlet.getAuthTokenFromCookie(request, response)).thenReturn(authToken);
    when(itemId.getAccountId()).thenReturn("accountId");
    when(itemId.getId()).thenReturn(27310);
    when(itemId.isLocal()).thenReturn(true);
    when(itemIdFactory.create("27310", "accountId")).thenThrow(AccountServiceException.NO_SUCH_ACCOUNT("AccountId"));

    previewHandler.handle(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,
        "Error processing requested attachment. Ensure message ID or account are correct.");
  }
}