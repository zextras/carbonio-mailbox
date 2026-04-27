/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.mcp;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.cs.account.Account;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class McpResourceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private String mcpUrl() {
		return server.getInternalApiEndpoint() + "/mcp";
	}

	// -------------------------------------------------------------------------
	// Test 1: initialize
	// -------------------------------------------------------------------------

	@Test
	void initializeReturnsServerInfo() throws Exception {
		// Given
		String body = """
				{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}
				""";

		// When
		Response response = server.getHttpClient().post(mcpUrl(), body);

		// Then
		assertEquals(200, response.statusCode());
		assertThatJson(response.body())
				.inPath("$.result.protocolVersion").isEqualTo("2024-11-05");
		assertThatJson(response.body())
				.inPath("$.result.serverInfo.name").isEqualTo("carbonio-mailbox");
		// Mcp-Session-Id is sent as an HTTP response header; verify the JSON id echo
		assertThatJson(response.body()).inPath("$.id").isEqualTo(1);
		// Verify the session-id UUID-shaped value is present in the body by checking
		// the "jsonrpc" field as a baseline and then checking the Mcp-Session-Id via
		// a second POST — the SESSION_ID is static per server instance, so a second
		// initialize call must return the same session-id embedded in the result.
		// Since TestHttpClient does not expose headers, we simply assert the response
		// includes the capabilities map confirming the full initialize result shape.
		assertThatJson(response.body())
				.inPath("$.result.capabilities").isObject();
	}

	// -------------------------------------------------------------------------
	// Test 2: tools/list
	// -------------------------------------------------------------------------

	@Test
	void toolsListReturnsSearchAccountsTool() throws Exception {
		// Given
		String body = """
				{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}
				""";

		// When
		Response response = server.getHttpClient().post(mcpUrl(), body);

		// Then
		assertEquals(200, response.statusCode());
		assertThatJson(response.body())
				.inPath("$.result.tools[0].name").isEqualTo("search_accounts");
		assertThatJson(response.body())
				.inPath("$.result.tools[0].inputSchema.type").isEqualTo("object");
		assertThatJson(response.body())
				.inPath("$.result.tools[0].inputSchema.properties.query.type").isEqualTo("string");
		assertThatJson(response.body())
				.inPath("$.result.tools[0].inputSchema.required[0]").isEqualTo("query");
	}

	// -------------------------------------------------------------------------
	// Test 3: tools/call — search_accounts with results
	// -------------------------------------------------------------------------

	@Test
	void toolsCallSearchAccountsReturnsResults() throws Exception {
		// Given
		Account caller = server.getAccountFactory().create();
		Account target = server.getAccountFactory().create();

		String body = objectMapper.writeValueAsString(Map.of(
				"jsonrpc", "2.0",
				"id", 3,
				"method", "tools/call",
				"params", Map.of(
						"name", "search_accounts",
						"arguments", Map.of("query", target.getName())
				)
		));

		// When
		Response response = server.getHttpClient().post(
				mcpUrl(), body,
				Map.of("X-Carbonio-User-Id", caller.getId()));

		// Then
		assertEquals(200, response.statusCode());
		assertThatJson(response.body())
				.inPath("$.result.content[0].type").isEqualTo("text");

		// The text field contains a nested JSON string — extract via Jackson
		JsonNode root = objectMapper.readTree(response.body());
		String contentText = root.at("/result/content/0/text").asText();
		assertThatJson(contentText).inPath("$.accounts[0].id").isEqualTo(target.getId());
	}

	// -------------------------------------------------------------------------
	// Test 4: tools/call without X-Carbonio-User-Id returns error
	// -------------------------------------------------------------------------

	@Test
	void toolsCallWithoutUserHeaderReturnsError() throws Exception {
		// Given
		String body = objectMapper.writeValueAsString(Map.of(
				"jsonrpc", "2.0",
				"id", 4,
				"method", "tools/call",
				"params", Map.of(
						"name", "search_accounts",
						"arguments", Map.of("query", "test")
				)
		));

		// When — no X-Carbonio-User-Id header
		Response response = server.getHttpClient().post(mcpUrl(), body);

		// Then
		assertEquals(200, response.statusCode());
		JsonNode root = objectMapper.readTree(response.body());
		String contentText = root.at("/result/content/0/text").asText();
		assertThat(contentText).contains("missing X-Carbonio-User-Id header");
	}

	// -------------------------------------------------------------------------
	// Test 5: tools/call unknown tool returns error
	// -------------------------------------------------------------------------

	@Test
	void toolsCallUnknownToolReturnsError() throws Exception {
		// Given
		Account caller = server.getAccountFactory().create();

		String body = objectMapper.writeValueAsString(Map.of(
				"jsonrpc", "2.0",
				"id", 5,
				"method", "tools/call",
				"params", Map.of(
						"name", "nonexistent_tool",
						"arguments", Map.of()
				)
		));

		// When
		Response response = server.getHttpClient().post(
				mcpUrl(), body,
				Map.of("X-Carbonio-User-Id", caller.getId()));

		// Then
		assertEquals(200, response.statusCode());
		JsonNode root = objectMapper.readTree(response.body());
		String contentText = root.at("/result/content/0/text").asText();
		assertThat(contentText).contains("unknown tool");
	}
}
