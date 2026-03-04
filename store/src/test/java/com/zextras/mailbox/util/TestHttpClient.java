/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

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
					.setSSLSocketFactory(socketFactory)
					.build();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Failed to create trust-all SSL context", e);
		}
	}

	public Response execute(HttpUriRequest request) throws IOException {
		try (CloseableHttpResponse response = delegate.execute(request)) {
			return new Response(
					response.getStatusLine().getStatusCode(),
					EntityUtils.toString(response.getEntity()));
		}
	}

	@Override
	public void close() throws Exception {
		delegate.close();
	}

	public record Response(int statusCode, String body) {}
}
