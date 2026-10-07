/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.carbonio.authz;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class AuthzClient {

  private final String baseUrl;
  private final HttpClient httpClient;
  private final Duration timeout;
  private final ObjectMapper objectMapper =
      new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  private AuthzClient(String baseUrl, HttpClient httpClient, Duration timeout) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.httpClient = httpClient;
    this.timeout = timeout;
  }

  public static Builder builder(String baseUrl) {
    return new Builder(baseUrl);
  }

  public boolean checkRight(RightSubject subject, String right, RightTarget target)
      throws AuthzException {
    final Map<String, Object> body = Map.of("subject", subject, "right", right, "target", target);
    final JsonNode response = send(post("/rights/check", body), type(JsonNode.class));
    return response.path("allowed").asBoolean();
  }

  public boolean checkRight(RightSubject subject, AuthzRight right, RightTarget target)
      throws AuthzException {
    return checkRight(subject, right.rightName(), target);
  }

  public List<RightInfo> listRights(AuthzTargetType targetType) throws AuthzException {
    return listRights(targetType.code());
  }

  public List<RightInfo> listRights(String targetType) throws AuthzException {
    final HttpRequest.Builder request = HttpRequest.newBuilder(
            URI.create(baseUrl + "/rights?targetType="
                + URLEncoder.encode(targetType, StandardCharsets.UTF_8)))
        .timeout(timeout)
        .GET();
    return send(request, objectMapper.getTypeFactory().constructCollectionType(List.class, RightInfo.class));
  }

  private HttpRequest.Builder post(String path, Object body) throws AuthzException {
    try {
      return HttpRequest.newBuilder(URI.create(baseUrl + path))
          .timeout(timeout)
          .header("Content-Type", "application/json")
          .POST(BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(body)));
    } catch (IOException e) {
      throw new AuthzException("Cannot serialize request to " + path, e);
    }
  }

  private <T> T send(HttpRequest.Builder requestBuilder, JavaType responseType)
      throws AuthzException {
    final HttpRequest request = requestBuilder.build();
    final HttpResponse<byte[]> response = execute(request);
    final int status = response.statusCode();
    if (status >= 200 && status < 300) {
      return read(request, response.body(), responseType);
    }
    final String message = request.method() + " " + request.uri().getPath() + " failed with HTTP "
        + status + errorDetail(response.body());
    throw switch (status) {
      case 400 -> new BadRequestException(message);
      case 401 -> new UnauthorizedException(message);
      default -> status >= 500 ? new ServerErrorException(message) : new AuthzException(message);
    };
  }

  private HttpResponse<byte[]> execute(HttpRequest request) throws AuthzException {
    try {
      return httpClient.send(request, BodyHandlers.ofByteArray());
    } catch (IOException e) {
      throw new AuthzException("Cannot reach " + request.uri(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AuthzException("Interrupted calling " + request.uri(), e);
    }
  }

  private <T> T read(HttpRequest request, byte[] body, JavaType responseType)
      throws AuthzException {
    try {
      return objectMapper.readValue(body, responseType);
    } catch (IOException e) {
      throw new AuthzException("Unexpected response from " + request.uri(), e);
    }
  }

  private String errorDetail(byte[] body) {
    try {
      final String error = objectMapper.readTree(body).path("error").asText("");
      return error.isEmpty() ? "" : ": " + error;
    } catch (IOException e) {
      return "";
    }
  }

  private JavaType type(Class<?> clazz) {
    return objectMapper.getTypeFactory().constructType(clazz);
  }

  public static class Builder {

    private final String baseUrl;
    private HttpClient httpClient;
    private Duration timeout = Duration.ofSeconds(10);

    private Builder(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public Builder withHttpClient(HttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    public Builder withTimeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    public AuthzClient build() {
      final HttpClient client = httpClient != null
          ? httpClient
          : HttpClient.newBuilder().connectTimeout(timeout).build();
      return new AuthzClient(baseUrl, client, timeout);
    }
  }
}
