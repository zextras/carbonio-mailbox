/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zextras.mailbox.api.rest.resource.dto.AccountSearchResponse;
import com.zextras.mailbox.api.rest.service.AccountService;
import io.vavr.control.Try;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class McpToolRegistry {

    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();

    public record ToolDefinition(
            String name,
            String description,
            ObjectNode inputSchema,
            BiFunction<Map<String, Object>, String, Object> handler
    ) {}

    public void register(ToolDefinition tool) {
        tools.put(tool.name(), tool);
    }

    public ToolDefinition get(String name) {
        return tools.get(name);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static McpToolRegistry withAccountSearch(AccountService accountService) {
        McpToolRegistry registry = new McpToolRegistry();
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        properties.putObject("query")
                .put("type", "string")
                .put("description", "Search query — matched against uid, display name, and email address");
        properties.putObject("limit")
                .put("type", "integer")
                .put("description", "Max results to return (default 10)");
        properties.putObject("offset")
                .put("type", "integer")
                .put("description", "Number of results to skip for pagination (default 0)");
        schema.putArray("required").add("query");

        registry.register(new ToolDefinition(
                "search_accounts",
                "Search for Carbonio user accounts by name or email. Returns account IDs, emails, and display names. Use this to resolve a person's name to their account UUID.",
                schema,
                (args, callerUserId) -> {
                    String query = (String) args.getOrDefault("query", "");
                    int limit = args.containsKey("limit") ? ((Number) args.get("limit")).intValue() : 10;
                    int offset = args.containsKey("offset") ? ((Number) args.get("offset")).intValue() : 0;
                    Try<AccountSearchResponse> result = accountService.searchAccounts(query, callerUserId, limit, offset);
                    if (result.isFailure()) {
                        throw new RuntimeException(result.getCause());
                    }
                    return result.get();
                }));
        return registry;
    }

    public List<Map<String, Object>> listTools() {
        return tools.values().stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", t.name());
                    m.put("description", t.description());
                    m.put("inputSchema", MAPPER.convertValue(t.inputSchema(), Map.class));
                    return m;
                })
                .toList();
    }
}
