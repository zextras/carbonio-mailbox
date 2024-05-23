package com.zextras.mailbox.tracking;

import static com.zextras.mailbox.tracking.PostHogTracking.SITE_KEY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.zimbra.cs.httpclient.HttpClientFactory;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;

class PostHogTrackingTest {

    private ClientAndServer postHog;
    private static final int POSTHOG_PORT = 5000;

    private final HttpClientFactory httpClientFactoryMock = mock(HttpClientFactory.class);

    private final PostHogTracking postHogTracking = new PostHogTracking("http://localhost:" + POSTHOG_PORT,
        httpClientFactoryMock);

    @BeforeEach
    public void startUp() throws Exception {
        when(httpClientFactoryMock.createWithProxy()).thenReturn(HttpClients.createMinimal());
        postHog = startClientAndServer(POSTHOG_PORT);
    }

    @AfterEach
    public void tearDown() throws Exception {
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

    @SuppressWarnings("SameParameterValue")
    private HttpRequest createRequest(String uid, String action) {
        return request()
                .withMethod("POST")
                .withPath("/capture/")
                .withBody("{\n" +
                        "        \"event\": \"" + action + "\",\n" +
                        "        \"api_key\": \"" + SITE_KEY + "\",\n" +
                        "        \"distinct_id\": \"" + uid + "\"\n" +
                        "        }");
    }
}
