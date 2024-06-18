package com.zimbra.cs.httpclient;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Factory class for creating instances of {@link CloseableHttpClient}.
 * This factory provides methods to create HTTP clients with various configurations.
 */
public class HttpClientFactory {

  /**
   * Creates a {@link CloseableHttpClient} instance configured with proxy settings.
   * <p>
   * This method uses the {@link HttpProxyUtil} class to configure proxy settings
   * for the HTTP client builder before building the {@link CloseableHttpClient}.
   * </p>
   *
   * @return a {@link CloseableHttpClient} instance configured with proxy settings
   */
  public CloseableHttpClient createWithProxy() {
    final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    HttpProxyUtil.configureProxy(httpClientBuilder);
    return httpClientBuilder.build();
  }
}

