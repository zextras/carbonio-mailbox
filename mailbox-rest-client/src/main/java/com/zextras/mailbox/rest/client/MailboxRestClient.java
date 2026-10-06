/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.rest.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class MailboxRestClient {

  private final String baseUrl;
  private final HttpClient httpClient;
  private final Duration timeout;
  private final ObjectMapper objectMapper =
      new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  private MailboxRestClient(String baseUrl, HttpClient httpClient, Duration timeout) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.httpClient = httpClient;
    this.timeout = timeout;
  }

  public static Builder builder(String baseUrl) {
    return new Builder(baseUrl);
  }

  public Account getAccount(String authToken, String accountId) throws MailboxRestException {
    final HttpRequest request = HttpRequest.newBuilder(
            URI.create(baseUrl + "/accounts/" + encode(accountId) + "/info"))
        .timeout(timeout)
        .header("Cookie", "ZM_AUTH_TOKEN=" + authToken)
        .GET()
        .build();
    return send(request, Account.class);
  }

  private <T> T send(HttpRequest request, Class<T> responseType) throws MailboxRestException {
    final HttpResponse<byte[]> response = execute(request);
    final int status = response.statusCode();
    if (status >= 200 && status < 300) {
      return read(request, response.body(), responseType);
    }
    final String message = request.method() + " " + request.uri().getPath() + " failed with HTTP "
        + status + errorDetail(response.body());
    throw switch (status) {
      case 401 -> new UnauthorizedException(message);
      case 403 -> new ForbiddenException(message);
      case 404 -> new NotFoundException(message);
      default -> status >= 500 ? new ServerErrorException(message) : new MailboxRestException(message);
    };
  }

  private HttpResponse<byte[]> execute(HttpRequest request) throws MailboxRestException {
    try {
      return httpClient.send(request, BodyHandlers.ofByteArray());
    } catch (IOException e) {
      throw new MailboxRestException("Cannot reach " + request.uri(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MailboxRestException("Interrupted calling " + request.uri(), e);
    }
  }

  private <T> T read(HttpRequest request, byte[] body, Class<T> responseType)
      throws MailboxRestException {
    try {
      return objectMapper.readValue(body, responseType);
    } catch (IOException e) {
      throw new MailboxRestException("Unexpected response from " + request.uri(), e);
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

    public MailboxRestClient build() {
      final HttpClient client = httpClient != null
          ? httpClient
          : HttpClient.newBuilder().connectTimeout(timeout).build();
      return new MailboxRestClient(baseUrl, client, timeout);
    }
  }
}
