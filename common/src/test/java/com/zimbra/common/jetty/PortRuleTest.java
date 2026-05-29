/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.common.jetty;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PortRuleTest {

	private Server server;

	private static int findFreePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new RuntimeException("Unable to find a free port", e);
		}
	}

	@AfterEach
	void afterEach() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	private static final class TestServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			resp.setStatus(200);
			resp.getWriter().write("ok");
		}
	}

	private int startServerWithPortRule(int rulePort, String regex, int errorStatus) throws Exception {
		return startServerWithPortRule(rulePort, regex, errorStatus, null);
	}

	private int startServerWithPortRule(int rulePort, String regex, int errorStatus, String errorReason) throws Exception {
		server = new Server();
		final int serverPort = findFreePort();
		final ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverPort);
		connector.setHost("localhost");
		server.addConnector(connector);

		final RewriteHandler rewriteHandler = new RewriteHandler();
		final PortRule portRule = new PortRule();
		portRule.setPort(rulePort);
		if (regex != null) {
			portRule.setRegex(regex);
		}
		if (errorStatus > 0) {
			portRule.setHttpErrorStatusRegexNotMatched(errorStatus);
		}
		if (errorReason != null) {
			portRule.setHttpErrorReasonRegexNotMatched(errorReason);
		}
		rewriteHandler.addRule(portRule);

		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		final ServletHolder servlet = new ServletHolder(new TestServlet());
		context.addServlet(servlet, "/*");

		rewriteHandler.setHandler(context.getCoreContextHandler());
		server.setHandler(rewriteHandler);
		server.start();
		return serverPort;
	}

	@Test
	void portRuleShouldMatchSamePort() throws Exception {
		final int serverPort = findFreePort();
		final int port = startServerWithPortRule(serverPort, ".*", 0);
		final HttpClient client = HttpClient.newHttpClient();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/test"))
				.build();
		final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode());
	}

	@Test
	void portRuleShouldNotMatchDifferentPort() throws Exception {
		final int port = startServerWithPortRule(9999, null, 0);
		final HttpClient client = HttpClient.newHttpClient();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/test"))
				.build();
		final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode());
	}

	@Test
	void portRuleShouldReturnErrorWhenPathDoesNotMatchRegex() throws Exception {
		final int serverPort = findFreePort();
		server = new Server();
		final ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverPort);
		connector.setHost("localhost");
		server.addConnector(connector);

		final RewriteHandler rewriteHandler = new RewriteHandler();
		final PortRule portRule = new PortRule();
		portRule.setPort(serverPort);
		portRule.setRegex("/allowed/.*");
		portRule.setHttpErrorStatusRegexNotMatched(403);
		rewriteHandler.addRule(portRule);

		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		final ServletHolder servlet = new ServletHolder(new TestServlet());
		context.addServlet(servlet, "/*");

		rewriteHandler.setHandler(context.getCoreContextHandler());
		server.setHandler(rewriteHandler);
		server.start();

		final HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + serverPort + "/disallowed"))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(403, response.statusCode());

		request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + serverPort + "/allowed/test"))
				.build();
		response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode());
	}

	@Test
	void portRuleShouldReturnCustomErrorWhenPathDoesNotMatchRegex() throws Exception {
		final int serverPort = findFreePort();
		server = new Server();
		final ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverPort);
		connector.setHost("localhost");
		server.addConnector(connector);

		final RewriteHandler rewriteHandler = new RewriteHandler();
		final PortRule portRule = new PortRule();
		portRule.setPort(serverPort);
		portRule.setRegex("/admin/.*");
		portRule.setHttpErrorStatusRegexNotMatched(404);
		rewriteHandler.addRule(portRule);

		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		final ServletHolder servlet = new ServletHolder(new TestServlet());
		context.addServlet(servlet, "/*");

		rewriteHandler.setHandler(context.getCoreContextHandler());
		server.setHandler(rewriteHandler);
		server.start();

		final HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + serverPort + "/non-admin"))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(404, response.statusCode());
	}

	@Test
	void portRuleShouldReturnCustomReasonWhenPathDoesNotMatchRegex() throws Exception {
		final int serverPort = findFreePort();
		server = new Server();
		final ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverPort);
		connector.setHost("localhost");
		server.addConnector(connector);

		final RewriteHandler rewriteHandler = new RewriteHandler();
		final PortRule portRule = new PortRule();
		portRule.setPort(serverPort);
		portRule.setRegex("/onlythis/.*");
		portRule.setHttpErrorStatusRegexNotMatched(403);
		portRule.setHttpErrorReasonRegexNotMatched("err_op_tob");
		rewriteHandler.addRule(portRule);

		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		final ServletHolder servlet = new ServletHolder(new TestServlet());
		context.addServlet(servlet, "/*");

		rewriteHandler.setHandler(context.getCoreContextHandler());
		server.setHandler(rewriteHandler);
		server.start();

		final HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + serverPort + "/wrong-path"))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(403, response.statusCode());
	}

	@Test
	void portRuleShouldHaveCorrectToString() {
		final PortRule rule = new PortRule();
		rule.setPort(8080);
		rule.setRegex("/test/.*");
		rule.setHttpErrorStatusRegexNotMatched(403);
		final String str = rule.toString();
		assertTrue(str.contains("8080"));
		assertTrue(str.contains("403"));
	}

	@Test
	void portRuleShouldHandleInvalidReasonKey() throws Exception {
		final int serverPort = findFreePort();
		server = new Server();
		final ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverPort);
		connector.setHost("localhost");
		server.addConnector(connector);

		final RewriteHandler rewriteHandler = new RewriteHandler();
		final PortRule portRule = new PortRule();
		portRule.setPort(serverPort);
		portRule.setRegex("/valid/.*");
		portRule.setHttpErrorStatusRegexNotMatched(400);
		portRule.setHttpErrorReasonRegexNotMatched("non_existent_key_xyz");
		rewriteHandler.addRule(portRule);

		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		final ServletHolder servlet = new ServletHolder(new TestServlet());
		context.addServlet(servlet, "/*");

		rewriteHandler.setHandler(context.getCoreContextHandler());
		server.setHandler(rewriteHandler);
		server.start();

		final HttpClient client = HttpClient.newHttpClient();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + serverPort + "/invalid"))
				.build();
		final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(400, response.statusCode());
	}

	@Test
	void portRuleShouldReturnCustomReasonForValidKey() throws Exception {
		final int serverPort = findFreePort();
		server = new Server();
		final ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverPort);
		connector.setHost("localhost");
		server.addConnector(connector);

		final RewriteHandler rewriteHandler = new RewriteHandler();
		final PortRule portRule = new PortRule();
		portRule.setPort(serverPort);
		portRule.setRegex("/valid/.*");
		portRule.setHttpErrorStatusRegexNotMatched(403);
		portRule.setHttpErrorReasonRegexNotMatched("replySubjectPrefix");
		rewriteHandler.addRule(portRule);

		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		final ServletHolder servlet = new ServletHolder(new TestServlet());
		context.addServlet(servlet, "/*");

		rewriteHandler.setHandler(context.getCoreContextHandler());
		server.setHandler(rewriteHandler);
		server.start();

		final HttpClient client = HttpClient.newHttpClient();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + serverPort + "/invalid"))
				.build();
		final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(403, response.statusCode());
	}
}
