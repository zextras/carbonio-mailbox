package com.zextras.mailbox.tracking;

import com.zimbra.cs.httpclient.HttpClientFactory;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class PostHogTracking implements Tracking {

    static final String SITE_KEY = "phc_egpFZ14OKByQMK51wCTzYp8tLrg0VA8wa2QDagXCjDG";
    private final String endPoint;

    private final HttpClientFactory httpClientFactory;

    public PostHogTracking(String endPoint, HttpClientFactory httpClientFactory) {
        this.endPoint = endPoint;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void sendEventIgnoringFailure(Event event) {
        try (var client = httpClientFactory.createWithProxy()) {
            client.execute(generateRequest(event));
        } catch (Exception ignored) {
        }
    }

    private HttpPost generateRequest(Event event) throws UnsupportedEncodingException {
        String url = endPoint + "/capture/";
        final HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity("{\n" +
                "        \"event\": \"" + event.getAction() + "\",\n" +
                "        \"api_key\": \"" + SITE_KEY + "\",\n" +
                "        \"distinct_id\": \"" + event.getUserId() + "\"\n" +
                "        }"));
        return httpPost;
    }
}
