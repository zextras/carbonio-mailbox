package com.zextras.mailbox.tracking;

import static com.zextras.mailbox.tracking.PostHogTracking.POSTHOG_API_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;

class PostHogTrackingTest {

    private ClientAndServer postHog;
    private static final int POSTHOG_PORT = 5000;
    private final PostHogTracking postHogTracking = new PostHogTracking("http://localhost:" + POSTHOG_PORT);

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

        assertDoesNotThrow(() -> postHogTracking.sendEventIgnoringFailure(event));
        postHog.verify(postHogRequest, VerificationTimes.exactly(1));
    }

    @Test
    void shouldNotFailWhenPostHogFails() {
        final Event event = new Event("UserId", "TestCategory", "TestAction");
        final HttpRequest postHogRequest = createRequest("UserId",  "TestAction");
        mockFailureResponse(postHogRequest);

        assertDoesNotThrow(() -> postHogTracking.sendEventIgnoringFailure(event));
        postHog.verify(postHogRequest, VerificationTimes.exactly(1));
    }

    private void mockFailureResponse(HttpRequest request) {
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
