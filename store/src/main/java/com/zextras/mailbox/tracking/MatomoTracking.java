// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracking;

import java.net.URI;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class MatomoTracking implements Tracking {

  private final String matomoEndpoint;

  public MatomoTracking(String matomoEndpoint) {
    this.matomoEndpoint = matomoEndpoint;
  }

  @Override
  public void sendEventIgnoringFailure(Event event) {
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      client.execute(generateRequest(event));
    } catch (Exception ignored) {
    }
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
