// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.resource;

import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.enums.Format;
import com.zextras.mailbox.filter.AuthorizationFilter;
import com.zextras.mailbox.preview.usecase.PreviewUseCase;
import com.zextras.mailbox.resource.acceptance.MailboxJerseyTest;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePart;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import org.apache.http.HttpHeaders;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PreviewControllerTest extends MailboxJerseyTest {

  private static final String TEST_ACCOUNT_NAME = "test@example.com";
  private PreviewUseCase previewUseCase;
  private Provisioning provisioning;

  /**
   * Provides arguments as: messageId, area, type, wanted output format, if thumbnail endpoint, if
   * want attachment, display inline or not
   *
   * @return arguments for Preview test
   */
  private static Stream<Arguments> getAttachments() {
    return Stream.of(
        Arguments.of("/preview/pdf/abcdef:1/2", Format.JPEG, true),
        Arguments.of("/preview/image/abcdef:1/2/0x0", Format.GIF, true),
        Arguments.of("/preview/document/abcdef:1/2", Format.JPEG, true),
        Arguments.of("/preview/document/abcdef:1/2/thumbnail", Format.JPEG, true));
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @Override
  protected DeploymentContext configureDeployment() {

    final ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.register(AuthorizationFilter.class);
    previewUseCase = mock(PreviewUseCase.class);
    resourceConfig.register(new PreviewController(previewUseCase));
    return ServletDeploymentContext.forServlet(new ServletContainer(resourceConfig)).build();
  }

  @BeforeEach
  public void beforeAll() throws Exception {
    MailboxTestUtil.initServer();
    provisioning = Provisioning.getInstance();
    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    provisioning.createAccount(TEST_ACCOUNT_NAME, "secret", attrs);
  }

  /**
   * Behaves like Preview service. Returns mime type from requested output format.
   *
   * @param outputFormat see {@link Format}
   * @return mime type for format
   */
  private String getContentTypeFromOutputFormat(Format outputFormat) {
    switch (outputFormat) {
      case GIF:
        return ContentType.IMAGE_GIF.getMimeType();
      case JPEG:
        return ContentType.IMAGE_JPEG.getMimeType();
      case PNG:
        return ContentType.IMAGE_PNG.getMimeType();
      default:
        return ContentType.IMAGE_JPEG.getMimeType();
    }
  }

  @ParameterizedTest
  @MethodSource("getAttachments")
  @DisplayName("Check Preview response is same from Preview service")
  public void shouldReturnPreviewWithWhenRequestingAttachmentPreview(
      String endpoint, Format outputFormat, boolean isInline) throws Exception {
    final String fileName = "attachment.txt";
    final String file = this.getClass().getResource(fileName).getFile();
    final byte[] previewContent = this.getClass().getResourceAsStream(fileName).readAllBytes();

    // preview response
    final InputStreamEntity inputStreamEntity =
        new InputStreamEntity(new ByteArrayInputStream(previewContent));
    inputStreamEntity.setContentType(getContentTypeFromOutputFormat(outputFormat));
    final BlobResponse previewResponse = new BlobResponse(inputStreamEntity);

    // attachment from  mailbox
    final MimeBodyPart attachment = new MimeBodyPart();
    attachment.attachFile(file);
    attachment.setFileName(fileName);

    final Tuple2<MimePart, BlobResponse> attachmentAndPreview =
        new Tuple2<>(attachment, previewResponse);

    final Account accountByName = provisioning.getAccountByName(TEST_ACCOUNT_NAME);
    final AuthToken authToken = AuthProvider.getAuthToken(accountByName);

    when(previewUseCase.getAttachmentAndPreview(
            anyString(), any(), any(), anyInt(), anyString(), any()))
        .thenReturn(Try.of(() -> attachmentAndPreview));

    final String disposition = isInline ? "inline" : "attachment";
    WebTarget target =
        target(endpoint).queryParam("output_format", outputFormat).queryParam("disp", disposition);
    final Response response =
        target.request().cookie(new Cookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded())).get();
    final String expectedContentDisposition =
        disposition + "; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    final int statusCode = response.getStatus();
    final InputStream receivedContent = response.readEntity(InputStream.class);

    assertEquals(HttpStatus.SC_OK, statusCode);
    assertArrayEquals(previewContent, receivedContent.readAllBytes());
    assertEquals(previewResponse.getMimeType(), response.getHeaderString(HttpHeaders.CONTENT_TYPE));
    assertEquals(expectedContentDisposition, response.getHeaderString(CONTENT_DISPOSITION));
  }
}
