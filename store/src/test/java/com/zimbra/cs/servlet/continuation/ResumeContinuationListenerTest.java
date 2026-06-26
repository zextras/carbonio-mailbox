/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.servlet.continuation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.util.PortUtil;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class ResumeContinuationListenerTest {

	private Server server;

	@AfterEach
	void afterEach() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	@Test
	void shouldCreateFromServletRequestFactory() {
		assertDoesNotThrow(() -> {
			final ResumeContinuationListener listener = new ResumeContinuationListener(
					new SimpleAsyncContext());
			assertNotNull(listener);
			assertNotNull(listener.getAsyncContext());
		});
	}

	@Test
	void resumeIfSuspendedShouldNotThrowWhenNotReady() {
		final ResumeContinuationListener listener = new ResumeContinuationListener(
				new SimpleAsyncContext());
		assertDoesNotThrow(listener::resumeIfSuspended);
	}

	@Test
	void onCompleteShouldResetReadyFlag() {
		final ResumeContinuationListener listener = new ResumeContinuationListener(
				new SimpleAsyncContext());
		listener.suspendAndUndispatch(5000);
		listener.onComplete(new AsyncEvent(new SimpleAsyncContext()));
		assertDoesNotThrow(listener::resumeIfSuspended);
	}

	@Test
	void onTimeoutShouldResetReadyFlag() {
		final ResumeContinuationListener listener = new ResumeContinuationListener(
				new SimpleAsyncContext());
		listener.suspendAndUndispatch(5000);
		listener.onTimeout(new AsyncEvent(new SimpleAsyncContext()));
		assertDoesNotThrow(listener::resumeIfSuspended);
	}

	@Test
	void onErrorShouldResetReadyFlag() {
		final ResumeContinuationListener listener = new ResumeContinuationListener(
				new SimpleAsyncContext());
		listener.suspendAndUndispatch(5000);
		listener.onError(new AsyncEvent(new SimpleAsyncContext()));
		assertDoesNotThrow(listener::resumeIfSuspended);
	}

	@Test
	void suspendAndUndispatchShouldSetTimeout() {
		final SimpleAsyncContext asyncContext = new SimpleAsyncContext();
		final ResumeContinuationListener listener = new ResumeContinuationListener(asyncContext);
		listener.suspendAndUndispatch(3000);
		assertEquals(3000, asyncContext.getTimeout());
	}

	@Test
	void resumeIfSuspendedShouldDispatchWhenReady() {
		final SimpleAsyncContext asyncContext = new SimpleAsyncContext();
		final ResumeContinuationListener listener = new ResumeContinuationListener(asyncContext);
		listener.suspendAndUndispatch(5000);
		listener.resumeIfSuspended();
		assertTrue(asyncContext.wasDispatched());
	}

	@Test
	void shouldCreateListenerWithRealAsyncContext() throws Exception {
		server = new Server();
		final int port = PortUtil.findFreePort();
		final ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		connector.setHost("localhost");
		server.addConnector(connector);

		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(AsyncLifecycleServlet.class), "/*");
		server.setHandler(context.getCoreContextHandler());
		server.start();

		final HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/async"))
				.timeout(Duration.ofSeconds(10))
				.build();

		final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode());
	}

	public static class AsyncLifecycleServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			final AsyncContext asyncContext = req.startAsync();
			assertNotNull(asyncContext);
			final ResumeContinuationListener listener = new ResumeContinuationListener(asyncContext);
			assertNotNull(listener.getAsyncContext());
			asyncContext.complete();
			resp.setStatus(200);
		}
	}

	private static class SimpleAsyncContext implements AsyncContext {
		private long timeout;
		private boolean dispatched = false;

		@Override
		public ServletRequest getRequest() { return null; }

		@Override
		public ServletResponse getResponse() { return null; }

		@Override
		public boolean hasOriginalRequestAndResponse() { return false; }

		@Override
		public void dispatch() { this.dispatched = true; }

		public boolean wasDispatched() { return dispatched; }

		@Override
		public void dispatch(String path) { this.dispatched = true; }

		@Override
		public void dispatch(javax.servlet.ServletContext context, String path) { this.dispatched = true; }

		@Override
		public void complete() {}

		@Override
		public void start(Runnable run) {}

		@Override
		public void addListener(javax.servlet.AsyncListener listener) {}

		@Override
		public void addListener(javax.servlet.AsyncListener listener,
				ServletRequest servletRequest, ServletResponse servletResponse) {}

		@Override
		public <T extends javax.servlet.AsyncListener> T createListener(Class<T> clazz) { return null; }

		@Override
		public void setTimeout(long timeout) { this.timeout = timeout; }

		@Override
		public long getTimeout() { return timeout; }
	}
}
