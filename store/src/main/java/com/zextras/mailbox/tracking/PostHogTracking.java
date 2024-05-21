package com.zextras.mailbox.tracking;

import com.zimbra.cs.httpclient.HttpClientFactory;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

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
        final HttpPost httpPost = new HttpPost();
        String url = endPoint + "/capture/";
        httpPost.setEntity(new StringEntity("{\n" +
                "        \"event\": \"" + event.getAction() + "\",\n" +
                "        \"api_key\": \"" + SITE_KEY + "\",\n" +
                "        \"distinct_id\": \"" + event.getUserId() + "\"\n" +
                "        }"));
        httpPost.setURI(URI.create(url));
        return httpPost;
    }
}
