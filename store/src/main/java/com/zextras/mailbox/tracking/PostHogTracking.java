package com.zextras.mailbox.tracking;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class PostHogTracking implements Tracking {

    static final String POSTHOG_API_KEY = "phc_egpFZ14OKByQMK51wCTzYp8tLrg0VA8wa2QDagXCjDG";
    private final String endPoint;

    public PostHogTracking(String endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public void sendEventIgnoringFailure(Event event) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            client.execute(generateRequest(event));
        } catch (Exception ignored) {
        }
    }

    private HttpPost generateRequest(Event event) throws UnsupportedEncodingException {
        final HttpPost httpPost = new HttpPost();
        String url = endPoint + "/capture/";
        httpPost.setEntity(new StringEntity("{\n" +
                "        \"event\": \"" + event.getAction() + "\",\n" +
                "        \"api_key\": \"" + POSTHOG_API_KEY + "\",\n" +
                "        \"distinct_id\": \"" + event.getUserId() + "\"\n" +
                "        }"));
        httpPost.setURI(URI.create(url));
        return httpPost;
    }
}
