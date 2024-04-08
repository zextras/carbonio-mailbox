// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracking;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.vavr.control.Try;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.Parameter;
import org.mockserver.verify.VerificationTimes;

class MatomoTrackingTest {

  private ClientAndServer matomo;
  private static final int MATOMO_PORT = 5000;

  @BeforeEach
  public void startUp() throws IOException {
    matomo = startClientAndServer(MATOMO_PORT);
  }

  @AfterEach
  public void tearDown() throws IOException {
    matomo.stop();
  }

  @Test
  void shouldSendEventToMatomo() {
    final Event event = new Event("UserId", "TestCategory", "TestAction");
    final HttpRequest matomoRequest = createMatomoRequest("UserId", "TestCategory", "TestAction");
    mockSuccessMatomoResponse(matomoRequest);

    final Try<Void> response = new MatomoTracking("http://localhost:" + MATOMO_PORT).sendEvent(event);

    matomo.verify(matomoRequest, VerificationTimes.exactly(1));
    Assertions.assertTrue(response.isSuccess());
  }

  @Test
  void shouldFailWhenMatomoFails() {
    final Event event = new Event("UserId", "TestCategory", "TestAction");
    final HttpRequest matomoRequest = createMatomoRequest("UserId", "TestCategory", "TestAction");
    mockFailureMatomoResponse(matomoRequest);

    final Try<Void> response = new MatomoTracking("http://localhost:" + MATOMO_PORT).sendEvent(event);

    matomo.verify(matomoRequest, VerificationTimes.exactly(1));
    Assertions.assertTrue(response.isFailure());
  }

  private void mockFailureMatomoResponse(HttpRequest request) {
    matomo
        .when(request)
        .respond(response().withStatusCode(500));
  }

  private void mockSuccessMatomoResponse(HttpRequest request) {
    matomo
        .when(request)
        .respond(response().withStatusCode(204));
  }

  private HttpRequest createMatomoRequest(String uid, String category, String action) {
    return request()
        .withMethod("GET")
        .withPath("/matomo.php")
        .withQueryStringParameters(
            Parameter.param("idsite", "7"),
            Parameter.param("rec", "1"),
            Parameter.param("send_image", "0"),
            Parameter.param("apiv", "1"),
            Parameter.param("e_c", category),
            Parameter.param("e_a", action),
            Parameter.param("uid", uid)
        );
  }
}