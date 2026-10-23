/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import com.zimbra.common.httpclient.HttpClientUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;

/**
 * Lightweight HTTP client wrapper for tests. Supports both plain HTTP and HTTPS with
 * self-signed certificates. Implements {@link AutoCloseable} so it can be managed as a
 * field with {@code @BeforeEach/@AfterEach} or used in try-with-resources.
 */
public class TestHttpClient implements AutoCloseable {

	private final CloseableHttpClient delegate;

	public TestHttpClient() {
		try {
			final SSLContext sslContext = SSLContextBuilder.create()
					.loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
					.build();
			final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
					sslContext, NoopHostnameVerifier.INSTANCE);
			this.delegate = HttpClientBuilder.create()
					.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
							.setSSLSocketFactory(socketFactory)
							.build())
					.build();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Failed to create trust-all SSL context", e);
		}
	}

	private Response execute(HttpUriRequest request) throws IOException {
		try (CloseableHttpResponse response = delegate.execute(request)) {
			return new Response(
					response.getCode(),
					HttpClientUtil.entityToString(response.getEntity()));
		}
	}

	public Response get(String url) throws IOException {
		return execute(new HttpGet(url));
	}

	public Response get(String url, Map<String, String> headers) throws IOException {
		final HttpGet request = new HttpGet(url);
		headers.forEach(request::setHeader);
		return execute(request);
	}

	public Response post(String url, String jsonBody) throws IOException {
		final HttpPost request = new HttpPost(url);
		request.setHeader("Content-Type", "application/json");
		request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
		return execute(request);
	}

	public Response post(String url, String jsonBody, Map<String, String> headers) throws IOException {
		final HttpPost request = new HttpPost(url);
		request.setHeader("Content-Type", "application/json");
		headers.forEach(request::setHeader);
		request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
		return execute(request);
	}

	@Override
	public void close() throws Exception {
		delegate.close();
	}

	public record Response(int statusCode, String body) {}
}
