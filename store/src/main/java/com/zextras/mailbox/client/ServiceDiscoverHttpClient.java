// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.exceptions.InternalServerError;
import com.zextras.carbonio.files.exceptions.UnAuthorized;
import io.vavr.control.Try;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServiceDiscoverHttpClient {

  private final Logger logger = LoggerFactory.getLogger(ServiceDiscoverHttpClient.class);

  private final String serviceDiscoverURL;
  private String token;

  ServiceDiscoverHttpClient(String serviceDiscoverURL) {
    this.serviceDiscoverURL = serviceDiscoverURL;
    this.token = System.getenv("CONSUL_HTTP_TOKEN"); // default: get from env
  }

  public static ServiceDiscoverHttpClient defaultUrl() {
    return new ServiceDiscoverHttpClient("http://localhost:8500");
  }

  public ServiceDiscoverHttpClient withToken(String token) {
    this.token = token;
    return this;
  }

  public Try<String> getConfig(String serviceName, String configKey) {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String url = serviceDiscoverURL + "/v1/kv/" + serviceName + "/" + configKey;
      HttpGet request = new HttpGet(url);
      request.setHeader("X-Consul-Token", token);
      try(CloseableHttpResponse response = httpClient.execute(request)) {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          String bodyResponse = IOUtils.toString(
              response.getEntity().getContent(),
              StandardCharsets.UTF_8
          );

          String value = new ObjectMapper().readTree(bodyResponse).get(0).get("Value").asText();
          String valueDecoded = new String(Base64.decodeBase64(value), StandardCharsets.UTF_8).trim();

          return Try.success(valueDecoded);
        }

        logger.error("Service discover didn't respond with 200 when requesting a config (received {})",
            response.getStatusLine().getStatusCode());
        return Try.failure(new UnAuthorized());
      }
    } catch (IOException exception) {
      logger.error("Exception trying to get config from service discover: ", exception);
      return Try.failure(new InternalServerError(exception));
    }
  }

  public Try<Boolean> isServiceInstalled(String serviceName) {
    String url = serviceDiscoverURL + "/v1/health/checks/" + serviceName;
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      HttpGet request = new HttpGet(url);
      request.setHeader("X-Consul-Token", token);
      try (CloseableHttpResponse response = httpClient.execute(request)) {
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          logger.error("Unexpected response status: {}", response.getStatusLine().getStatusCode());
          return Try.failure(new InternalServerError(new Exception("Unexpected response status: " + response.getStatusLine().getStatusCode())));
        }
        String bodyResponse = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        return Try.success(!"[]".equals(bodyResponse));
      }
    } catch (IOException exception) {
      logger.error("Exception trying to check if service is installed: ", exception);
      return Try.failure(new InternalServerError(exception));
    }
  }
}
