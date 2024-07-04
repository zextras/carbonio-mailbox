// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.exceptions.InternalServerError;
import com.zextras.carbonio.files.exceptions.UnAuthorized;
import com.zimbra.common.util.ZimbraLog;
import io.vavr.control.Try;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServiceDiscoverHttpClient {

  private final String serviceDiscoverURL;
  private String token;

  ServiceDiscoverHttpClient(String serviceDiscoverURL) {
    this.serviceDiscoverURL = serviceDiscoverURL;
    this.token = System.getenv("CONSUL_HTTP_TOKEN"); // default: get from env
  }

  public static ServiceDiscoverHttpClient atURL(
    String url,
    String serviceName
  ) {
    return new ServiceDiscoverHttpClient(url + "/v1/kv/" + serviceName + "/");
  }

  public static ServiceDiscoverHttpClient defaultURL(String serviceName) {
    return new ServiceDiscoverHttpClient("http://localhost:8500/v1/kv/" + serviceName + "/");
  }

  public ServiceDiscoverHttpClient withToken(String token) {
    this.token = token;
    return this;
  }

  public Try<String> getConfig(String configKey) {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      HttpGet request = new HttpGet(serviceDiscoverURL + configKey);
      request.setHeader("X-Consul-Token", token);
      CloseableHttpResponse response = httpClient.execute(request);

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        String bodyResponse = IOUtils.toString(
          response.getEntity().getContent(),
          StandardCharsets.UTF_8
        );

        String value = new ObjectMapper().readTree(bodyResponse).get(0).get("Value").asText();
        String valueDecoded = new String(Base64.decodeBase64(value), StandardCharsets.UTF_8).trim();

        return Try.success(valueDecoded);
      }
      return Try.failure(new UnAuthorized());
    } catch (IOException exception) {
      return Try.failure(new InternalServerError(exception));
    }
  }
}
