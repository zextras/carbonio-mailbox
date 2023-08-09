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
import com.zextras.mailbox.resource.PreviewController;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.AuthProvider;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import javax.mail.internet.MimeBodyPart;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PreviewControllerAcceptanceTest extends JerseyTest {

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

  // do not name this setup()
  @BeforeEach
  public void before() throws Exception {
    super.setUp();
  }

  // do not name this tearDown()
  @AfterEach
  public void after() throws Exception {
    super.tearDown();
  }

  private static Stream<Arguments> getAttachment() {
    return Stream.of(Arguments.of("MrKrab.gif", "image"));
  }

  // TODO: test GIF, PNG, JPEG, DOC, PDF + all thumbnail

  @ParameterizedTest
  @MethodSource("getAttachment")
  public void shouldReturnGifPreviewWhenRequestingGifAttachment(String fileName, String type)
      throws Exception {
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
    when(previewClient.postPreviewOfImage(any(), any(), eq(fileName)))
        .thenReturn(Try.of(() -> previewResponse));

    final Account accountByName = provisioning.getAccountByName(TEST_ACCOUNT_NAME);
    final AuthToken authToken = AuthProvider.getAuthToken(accountByName);
    final Response response =
        target("/" + type + "/" + messageId + "/" + partNumber + "/")
            .queryParam("first_page", 1)
            .queryParam("last_page", 1)
            .request()
            .cookie(new Cookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded()))
            .get();
    final byte[] expectedContent = this.getClass().getResourceAsStream(fileName).readAllBytes();
    final int statusCode = response.getStatus();
    final InputStream content = response.readEntity(InputStream.class);
    assertEquals(HttpStatus.SC_OK, statusCode);
    assertArrayEquals(expectedContent, content.readAllBytes());
  }

  @Test
  public void shouldReturnGifThumbnailWhenRequestingGifAttachment() throws Exception {
    final String fileName = "MrKrab.gif";
    final InputStream gifAttachment = this.getClass().getResourceAsStream("/" + fileName);
    final int messageId = 100;
    final String partNumber = "2";

    final InputStreamEntity inputStreamEntity = new InputStreamEntity(gifAttachment);
    inputStreamEntity.setContentType(ContentType.IMAGE_GIF.getMimeType());

    final BlobResponse previewResponse = new BlobResponse(inputStreamEntity);

    final MimeBodyPart mimePart = new MimeBodyPart();
    mimePart.attachFile(this.getClass().getResource("/" + fileName).getFile());

    when(mockAttachmentService.getAttachment(any(), any(), eq(messageId), eq(partNumber)))
        .thenReturn(Try.of(() -> mimePart));
    when(previewClient.postThumbnailOfImage(any(), any(), eq(fileName)))
        .thenReturn(Try.of(() -> previewResponse));

    final Account accountByName = provisioning.getAccountByName(TEST_ACCOUNT_NAME);
    final AuthToken authToken = AuthProvider.getAuthToken(accountByName);
    final Response response =
        target("/image/" + messageId + "/" + partNumber + "/0x0/thumbnail/")
            .queryParam("first_page", 1)
            .queryParam("last_page", 1)
            .request()
            .cookie(new Cookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded()))
            .get();
    final byte[] expectedContent =
        this.getClass().getResourceAsStream("/" + fileName).readAllBytes();
    final int statusCode = response.getStatus();
    final InputStream content = response.readEntity(InputStream.class);
    assertEquals(HttpStatus.SC_OK, statusCode);
    assertArrayEquals(expectedContent, content.readAllBytes());
  }
}
