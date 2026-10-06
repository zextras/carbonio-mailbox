/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.internalapi.client;

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

public class MailboxInternalApiClient {

  private final String baseUrl;
  private final HttpClient httpClient;
  private final Duration timeout;
  private final ObjectMapper objectMapper =
      new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  private MailboxInternalApiClient(String baseUrl, HttpClient httpClient, Duration timeout) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.httpClient = httpClient;
    this.timeout = timeout;
  }

  public static Builder builder(String baseUrl) {
    return new Builder(baseUrl);
  }

  public Account getAccount(String accountId) throws InternalApiException {
    return send(get("/accounts/" + encode(accountId) + "/info"), type(Account.class));
  }

  public Account getAccountByEmail(String email) throws InternalApiException {
    return send(get("/accounts?email=" + encode(email)), type(Account.class));
  }

  public Account getMyAccount(String authToken) throws InternalApiException {
    return send(get("/accounts/myself").header("Cookie", "ZM_AUTH_TOKEN=" + authToken),
        type(Account.class));
  }

  public List<Account> getAccountsByIds(List<String> ids) throws InternalApiException {
    return send(post("/accounts/batch", Map.of("ids", ids)), listOf(Account.class));
  }

  public List<Account> getAccountsByEmails(List<String> emails) throws InternalApiException {
    return send(post("/accounts/batch", Map.of("emails", emails)), listOf(Account.class));
  }

  public List<SharedAccount> getSharedAccounts(String accountId) throws InternalApiException {
    return send(get("/accounts/" + encode(accountId) + "/shared-accounts"),
        listOf(SharedAccount.class));
  }

  public MailUsage getMailUsage(String accountId) throws InternalApiException {
    return send(get("/accounts/mail/usage/" + encode(accountId)), type(MailUsage.class));
  }

  public Cos getCos(String cosId) throws InternalApiException {
    return send(get("/cos/" + encode(cosId)), type(Cos.class));
  }

  public List<RightInfo> listRights(String targetType) throws InternalApiException {
    return send(get("/rights?targetType=" + encode(targetType)), listOf(RightInfo.class));
  }

  public boolean checkRight(RightSubject subject, String right, RightTarget target)
      throws InternalApiException {
    final Map<String, Object> body = Map.of("subject", subject, "right", right, "target", target);
    final JsonNode response = send(post("/rights/check", body), type(JsonNode.class));
    return response.path("allowed").asBoolean();
  }

  private HttpRequest.Builder get(String path) {
    return HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(timeout).GET();
  }

  private HttpRequest.Builder post(String path, Object body) throws InternalApiException {
    try {
      return HttpRequest.newBuilder(URI.create(baseUrl + path))
          .timeout(timeout)
          .header("Content-Type", "application/json")
          .POST(BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(body)));
    } catch (IOException e) {
      throw new InternalApiException("Cannot serialize request to " + path, e);
    }
  }

  private <T> T send(HttpRequest.Builder requestBuilder, JavaType responseType)
      throws InternalApiException {
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
      case 404 -> new NotFoundException(message);
      default -> status >= 500 ? new ServerErrorException(message) : new InternalApiException(message);
    };
  }

  private HttpResponse<byte[]> execute(HttpRequest request) throws InternalApiException {
    try {
      return httpClient.send(request, BodyHandlers.ofByteArray());
    } catch (IOException e) {
      throw new InternalApiException("Cannot reach " + request.uri(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InternalApiException("Interrupted calling " + request.uri(), e);
    }
  }

  private <T> T read(HttpRequest request, byte[] body, JavaType responseType)
      throws InternalApiException {
    try {
      return objectMapper.readValue(body, responseType);
    } catch (IOException e) {
      throw new InternalApiException("Unexpected response from " + request.uri(), e);
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

  private JavaType listOf(Class<?> clazz) {
    return objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
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

    public MailboxInternalApiClient build() {
      final HttpClient client = httpClient != null
          ? httpClient
          : HttpClient.newBuilder().connectTimeout(timeout).build();
      return new MailboxInternalApiClient(baseUrl, client, timeout);
    }
  }
}
