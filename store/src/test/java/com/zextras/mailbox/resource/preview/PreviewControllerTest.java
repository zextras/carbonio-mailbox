// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.preview;

import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.mailbox.filter.AuthorizationFilter;
import com.zextras.mailbox.resource.acceptance.MailboxJerseyTest;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePart;
import javax.servlet.http.HttpUtils;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.logging.log4j.util.Strings;
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
import org.yaml.snakeyaml.util.UriEncoder;

class PreviewControllerTest extends MailboxJerseyTest {

  private static final String TEST_ACCOUNT_NAME = "test@example.com";
  private PreviewService previewService;
  private Provisioning provisioning;

  /**
   * Provides arguments as: account uuid of attachment owner, image name, endpoint, area, if want
   * thumbnail, query params, if disposition inline
   *
   * @return arguments for Preview test
   */
  private static Stream<Arguments> getAttachment() {
    return Stream.of(
        Arguments.of(
            UUID.randomUUID().toString(),
            "MrKrab.gif",
            "image",
            "0x0",
            false,
            "disp=attachment",
            false),
        Arguments.of("", "MrKrab.gif", "image", "0x0", true, "", true),
        Arguments.of(
            "",
            "MrKrab.gif",
            "image",
            "0x0",
            false,
            "first_page=1&last_page=2&first_page=10&disp=attachment",
            false),
        Arguments.of(
            UUID.randomUUID().toString(),
            "Calcolo_del_fuso.JPEG",
            "image",
            "0x0",
            false,
            "disp=inline",
            true),
        Arguments.of(
            UUID.randomUUID().toString(),
            "Calcolo_del_fuso.JPEG",
            "image",
            "0x0",
            true,
            "quality=high",
            true),
        Arguments.of("", "In-CC0.pdf", "pdf", "0x0", false, "", true),
        Arguments.of("", "In-CC0.pdf", "pdf", "0x0", true, "", true));
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @Override
  protected DeploymentContext configureDeployment() {

    final ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.register(AuthorizationFilter.class);
    previewService = mock(PreviewService.class);
    resourceConfig.register(new PreviewController(previewService));
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

  private String getMimeTypeFromFileName(String filename) throws IOException, URISyntaxException {
    return Files.probeContentType(Path.of(this.getClass().getResource(filename).toURI()));
  }

  @ParameterizedTest
  @MethodSource("getAttachment")
  public void shouldReturnPreviewWhenRequestingAttachment(
      String accountId,
      String fileName,
      String type,
      String area,
      boolean isThumbNail,
      String query,
      boolean isInline)
      throws Exception {
    final InputStream gifAttachment = this.getClass().getResourceAsStream(fileName);
    final int messageId = 100;
    final String partNumber = "2";

    final InputStreamEntity inputStreamEntity = new InputStreamEntity(gifAttachment);
    inputStreamEntity.setContentType(ContentType.IMAGE_GIF.getMimeType());

    final BlobResponse previewResponse = new BlobResponse(inputStreamEntity);

    final MimeBodyPart mimePart = new MimeBodyPart();
    mimePart.attachFile(this.getClass().getResource(fileName).getFile());
    mimePart.setHeader(CONTENT_TYPE, getMimeTypeFromFileName(fileName));

    final Tuple2<MimePart, BlobResponse> attachmentAndPreview =
        new Tuple2<>(mimePart, previewResponse);

    final Account accountByName = provisioning.getAccountByName(TEST_ACCOUNT_NAME);
    final AuthToken authToken = AuthProvider.getAuthToken(accountByName);
    final String expectedAccountId =
        Strings.isEmpty(accountId) ? authToken.getAccountId() : accountId;

    when(previewService.getAttachmentAndPreview(
            eq(expectedAccountId), any(), any(), eq(messageId), eq(partNumber), any()))
        .thenReturn(Try.of(() -> attachmentAndPreview));

    final String thumbnailUrl = isThumbNail ? "thumbnail" : "";
    final String messageIdPath =
        Strings.isEmpty(accountId) ? String.valueOf(messageId) : accountId + ":" + messageId;

    WebTarget target =
        target(
            "/" + type + "/" + messageIdPath + "/" + partNumber + "/" + area + "/" + thumbnailUrl);
    target = addParams(target, query);
    final Response response =
        target.request().cookie(new Cookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded())).get();
    final byte[] expectedContent = this.getClass().getResourceAsStream(fileName).readAllBytes();
    final String expectedContentDisposition =
        (isInline ? "inline" : "attachment") + "; filename*=UTF-8''" + UriEncoder.encode(fileName);
    final int statusCode = response.getStatus();
    final InputStream content = response.readEntity(InputStream.class);
    assertEquals(HttpStatus.SC_OK, statusCode);
    assertArrayEquals(expectedContent, content.readAllBytes());
    assertEquals(mimePart.getContentType(), response.getHeaderString(HttpHeaders.CONTENT_TYPE));
    assertEquals(expectedContentDisposition, response.getHeaderString(CONTENT_DISPOSITION));
  }

  private WebTarget addParams(WebTarget webTarget, String paramString) {
    Map<String, String[]> paramsMap = HttpUtils.parseQueryString(paramString);
    for (Entry<String, String[]> entry : paramsMap.entrySet()) {
      webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
    }
    return webTarget;
  }
}
