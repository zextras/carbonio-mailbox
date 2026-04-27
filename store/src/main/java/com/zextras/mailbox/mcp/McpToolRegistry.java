/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public List<Map<String, Object>> listTools() {
        ObjectMapper mapper = new ObjectMapper();
        return tools.values().stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", t.name());
                    m.put("description", t.description());
                    m.put("inputSchema", mapper.convertValue(t.inputSchema(), Map.class));
                    return m;
                })
                .toList();
    }
}
