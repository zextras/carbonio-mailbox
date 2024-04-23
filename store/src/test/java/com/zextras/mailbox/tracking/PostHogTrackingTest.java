package com.zextras.mailbox.tracking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.Parameter;
import org.mockserver.verify.VerificationTimes;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class PostHogTrackingTest {


    private static final String POSTHOG_API_KEY = "phc_egpFZ14OKByQMK51wCTzYp8tLrg0VA8wa2QDagXCjDG";
    private ClientAndServer postHog;
    private static final int POSTHOG_PORT = 5000;

    @BeforeEach
    public void startUp() throws IOException {
        postHog = startClientAndServer(POSTHOG_PORT);
    }

    @AfterEach
    public void tearDown() throws IOException {
        postHog.stop();
    }

    @Test
    void shouldSendEventToPostHog() {
        final Event event = new Event("UserId", "TestCategory", "TestAction");
        final HttpRequest postHogRequest = createRequest("UserId",  "TestAction");
        mockSuccessResponse(postHogRequest);

        assertDoesNotThrow(() -> new PostHogTracking("http://localhost:" + POSTHOG_PORT).sendEventIgnoringFailure(event));
        postHog.verify(postHogRequest, VerificationTimes.exactly(1));
    }

//    @Test
//    void shouldFailWhenMatomoFails() {
//        final Event event = new Event("UserId", "TestCategory", "TestAction");
//        final HttpRequest matomoRequest = createMatomoRequest("UserId", "TestCategory", "TestAction");
//        mockFailureMatomoResponse(matomoRequest);
//
//        assertDoesNotThrow(() -> new MatomoTracking("http://localhost:" + MATOMO_PORT).sendEventIgnoringFailure(event));
//        matomo.verify(matomoRequest, VerificationTimes.exactly(1));
//    }

    private void mockFailureMatomoResponse(HttpRequest request) {
        postHog
                .when(request)
                .respond(response().withStatusCode(500));
    }

    private void mockSuccessResponse(HttpRequest request) {
        postHog
                .when(request)
                .respond(response().withStatusCode(200));
    }

    private HttpRequest createRequest(String uid, String action) {
        return request()
                .withMethod("POST")
                .withPath("/capture/")
                .withBody("{\n" +
                        "        \"event\": \"" + action + "\",\n" +
                        "        \"api_key\": \"" + POSTHOG_API_KEY + "\",\n" +
                        "        \"distinct_id\": \"" + uid + "\"\n" +
                        "        }");
    }
}
