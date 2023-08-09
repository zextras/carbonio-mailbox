// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.acceptance;

import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.mailbox.filter.AuthorizationFilter;
import com.zextras.mailbox.resource.preview.PreviewController;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.AuthProvider;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.HttpUtils;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PreviewControllerAcceptanceTest extends MailboxJerseyTest {

  private AttachmentService mockAttachmentService;
  private PreviewClient previewClient;

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @Override
  protected DeploymentContext configureDeployment() {

    final ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.register(AuthorizationFilter.class);
    previewClient = mock(PreviewClient.class);
    mockAttachmentService = mock(AttachmentService.class);
    resourceConfig.register(new PreviewController(mockAttachmentService, previewClient));
    return ServletDeploymentContext.forServlet(new ServletContainer(resourceConfig)).build();
  }

  private Provisioning provisioning;
  private static final String TEST_ACCOUNT_NAME = "test@example.com";

  @BeforeEach
  public void beforeAll() throws Exception {
    MailboxTestUtil.initServer();
    provisioning = Provisioning.getInstance();
    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    provisioning.createAccount(TEST_ACCOUNT_NAME, "secret", attrs);
  }

  /**
   * Provides arguments as: image name, type, thumbnail path, query params
   *
   * @return arguments for Preview test
   */
  private static Stream<Arguments> getAttachment() {
    return Stream.of(
        Arguments.of("MrKrab.gif", "image", "", ""),
        Arguments.of("MrKrab.gif", "image", "0x0/thumbnail", ""),
        Arguments.of(
            "MrKrab.gif", "image", "0x0/thumbnail", "?first_page=1&last_page=2&first_page=10"),
        Arguments.of("Calcolo_del_fuso.JPEG", "image", "", ""),
        Arguments.of("Calcolo_del_fuso.JPEG", "image", "0x0/thumbnail", "?quality=high"),
        Arguments.of("In-CC0.pdf", "pdf", "", ""),
        Arguments.of("In-CC0.pdf", "pdf", "0x0/thumbnail", ""));
  }

  /**
   * Mocks {@link PreviewClient} method to call by requested attachment type
   *
   * @param isThumbNail if should mock thumbnail method
   * @param fileName name of file
   * @param type type of file/attachment
   * @param response response to return from mock
   */
  private void mockPreviewByType(
      String fileName, String type, boolean isThumbNail, BlobResponse response) {
    switch (type) {
      case "image":
        if (isThumbNail) {
          when(previewClient.postThumbnailOfImage(any(), any(), eq(fileName)))
              .thenReturn(Try.of(() -> response));
          break;
        }
        when(previewClient.postPreviewOfImage(any(), any(), eq(fileName)))
            .thenReturn(Try.of(() -> response));
        break;
      case "pdf":
        if (isThumbNail) {
          when(previewClient.postThumbnailOfPdf(any(), any(), eq(fileName)))
              .thenReturn(Try.of(() -> response));
          break;
        }
        when(previewClient.postPreviewOfPdf(any(), any(), eq(fileName)))
            .thenReturn(Try.of(() -> response));
        break;
      case "doc":
        if (isThumbNail) {
          when(previewClient.postThumbnailOfDocument(any(), any(), eq(fileName)))
              .thenReturn(Try.of(() -> response));
          break;
        }
        when(previewClient.postPreviewOfDocument(any(), any(), eq(fileName)))
            .thenReturn(Try.of(() -> response));
        break;
      default:
        break;
    }
  }

  @ParameterizedTest
  @MethodSource("getAttachment")
  public void shouldReturnPreviewWhenRequestingAttachment(
      String fileName, String type, String optionalThumbnailUrl, String query) throws Exception {
    final InputStream gifAttachment = this.getClass().getResourceAsStream(fileName);
    final int messageId = 100;
    final String partNumber = "2";

    final InputStreamEntity inputStreamEntity = new InputStreamEntity(gifAttachment);
    inputStreamEntity.setContentType(ContentType.IMAGE_GIF.getMimeType());

    final BlobResponse previewResponse = new BlobResponse(inputStreamEntity);

    final MimeBodyPart mimePart = new MimeBodyPart();
    mimePart.attachFile(this.getClass().getResource(fileName).getFile());

    when(mockAttachmentService.getAttachment(any(), any(), eq(messageId), eq(partNumber)))
        .thenReturn(Try.of(() -> mimePart));
    this.mockPreviewByType(
        fileName, type, !Objects.equals("", optionalThumbnailUrl), previewResponse);

    final Account accountByName = provisioning.getAccountByName(TEST_ACCOUNT_NAME);
    final AuthToken authToken = AuthProvider.getAuthToken(accountByName);

    WebTarget target =
        target("/" + type + "/" + messageId + "/" + partNumber + "/" + optionalThumbnailUrl);
    target = addParams(target, query);
    final Response response =
        target.request().cookie(new Cookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded())).get();
    final byte[] expectedContent = this.getClass().getResourceAsStream(fileName).readAllBytes();
    final int statusCode = response.getStatus();
    final InputStream content = response.readEntity(InputStream.class);
    assertEquals(HttpStatus.SC_OK, statusCode);
    assertArrayEquals(expectedContent, content.readAllBytes());
  }

  private WebTarget addParams(WebTarget webTarget, String paramString) {
    Map<String, String[]> paramsMap = HttpUtils.parseQueryString(paramString);
    paramsMap.forEach((key, value) -> webTarget.queryParam(key, value));
    return webTarget;
  }
}
