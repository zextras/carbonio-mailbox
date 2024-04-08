// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracking;

import io.vavr.control.Try;
import java.net.URI;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class MatomoTracking implements Tracking {

  private final String matomoEndpoint;

  public MatomoTracking(String matomoEndpoint) {
    this.matomoEndpoint = matomoEndpoint;
  }

  @Override
  public Try<Void> sendEvent(Event event) {
    return Try.run(() -> {
      try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
        final CloseableHttpResponse execute = client.execute(generateRequest(event));
        final int statusCode = execute.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_NO_CONTENT) {
          throw new RuntimeException("failed");
        }
      }
    });
  }

  private HttpGet generateRequest(Event event) {
    final HttpGet httpGet = new HttpGet();
    String url = matomoEndpoint + "/matomo.php?idsite=7&rec=1&send_image=0&apiv=1"
        + "&e_c=" + event.getCategory() +
        "&e_a=" + event.getAction() +
        "&uid=" + event.getUserId();
    httpGet.setURI(URI.create(url));
    return httpGet;
  }

}
