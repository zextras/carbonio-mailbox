// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.files.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.FilesClient;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import io.vavr.control.Try;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;

class GraphQLFilesClientTest {

  private ClientAndServer filesServer;
  private static final int PORT = 20002;
  private FilesClient filesClient;


  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.initServer();
    filesServer = startClientAndServer(PORT);
    filesClient = FilesClient.atURL("http://127.0.0.1:" + PORT);
  }

  @AfterEach
  public void tearDown() throws IOException {
    filesServer.stop();
  }


  @Test
  void shouldCreateLink() {
    final String url = "http://fake.linkUrl.com";
    filesServer
        .when(request().withPath("/graphql/"))
        .respond(
            HttpResponse.response("{"
                    + "    \"data\": {"
                    + "        \"createLink\": {"
                    + "            \"id\": \"a4bbe479-1f42-49e4-8619-b6a068015dd5\","
                    + "            \"url\": \"" + url + "\","
                    + "            \"description\": \"ef196081-0330-43e3-93a3-e8241e3c7fb2\","
                    + "            \"expires_at\": null,"
                    + "            \"created_at\": 1709906611074,"
                    + "            \"node\": {"
                    + "                \"id\": \"ef196081-0330-43e3-93a3-e8241e3c7fb2\","
                    + "                \"__typename\": \"File\""
                    + "            },\n"
                    + "            \"__typename\": \"Link\""
                    + "        }"
                    + "    }"
                    + "}")
                .withStatusCode(200));
    final GraphQLFilesClient graphQLFilesClient = new GraphQLFilesClient(filesClient,
        new ObjectMapper());

    final Try<CreateLink> tryCreateLink = graphQLFilesClient.createLink(new Token("testToken"),
        "123Star");

    filesServer.verifyZeroInteractions();

    Assertions.assertTrue(tryCreateLink.isSuccess());
    Assertions.assertEquals(url, tryCreateLink.get().getUrl());
  }

  @Test
  void testCreateLinkMapper() throws Exception {
    final String url = "http://fake.linkUrl.com";
    final CreateLink createLink = GraphQLFilesClient.mapToCreateLink(new ObjectMapper(), "{"
        + "    \"data\": {"
        + "        \"createLink\": {"
        + "            \"id\": \"a4bbe479-1f42-49e4-8619-b6a068015dd5\","
        + "            \"url\": \"" + url + "\","
        + "            \"description\": \"ef196081-0330-43e3-93a3-e8241e3c7fb2\","
        + "            \"expires_at\": null,"
        + "            \"created_at\": 1709906611074,"
        + "            \"node\": {"
        + "                \"id\": \"ef196081-0330-43e3-93a3-e8241e3c7fb2\","
        + "                \"__typename\": \"File\""
        + "            },\n"
        + "            \"__typename\": \"Link\""
        + "        }"
        + "    }"
        + "}");
    Assertions.assertEquals(url, createLink.getUrl());
  }


}