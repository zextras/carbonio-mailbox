// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.files.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.FilesClient;
import io.vavr.control.Try;
import java.io.IOException;

public class GraphQLFilesClient {


  private final static String GQL_CREATE_LINK_REQUEST = "{\"operationName\":\"createLink\","
      + "\"variables\":"
      + "{\"node_id\":\"%s\","
      + "\"description\":\"\"},"
      + "\"query\":\"mutation createLink($node_id: ID!, $description: String, $expires_at: DateTime) "
      + "{\\n  createLink(\\n    node_id: $node_id\\n    description: $description\\n    expires_at: $expires_at\\n  ) "
      + "{\\n    ...Link\\n    __typename\\n  }\\n}\\n\\n"
      + "fragment Link on Link {\\n  id\\n  url\\n  description\\n  expires_at\\n  created_at\\n  "
      + "node {\\n    id\\n    __typename\\n  }\\n  __typename\\n}\"}";

  private final FilesClient filesClient;
  private final ObjectMapper objectMapper;

  public GraphQLFilesClient(FilesClient filesClient,
      ObjectMapper objectMapper) {
    this.filesClient = filesClient;
    this.objectMapper = objectMapper;
  }

  public static CreateLink mapToCreateLink(ObjectMapper objectMapper, String graphQLResponse)
      throws IOException {
    final GraphQlResponse gqlDTO = objectMapper.readValue(graphQLResponse, GraphQlResponse.class);
    return new CreateLink(gqlDTO.getData().get("createLink").get("url").asText());
  }

  public Try<CreateLink> createLink(Token authToken, String nodeId) {
    return this.filesClient.genericGraphQLRequest(
            authToken.getCookie(),
            String.format(GQL_CREATE_LINK_REQUEST, nodeId))
        .mapTry(response -> mapToCreateLink(objectMapper, response));
  }


}
