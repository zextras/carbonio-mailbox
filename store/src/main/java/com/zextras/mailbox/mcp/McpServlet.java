/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class McpServlet extends HttpServlet {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private final ObjectMapper mapper = new ObjectMapper();
    private final McpToolRegistry toolRegistry;

    public McpServlet(McpToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setHeader("Mcp-Session-Id", SESSION_ID);

        Map<String, Object> request = mapper.readValue(req.getInputStream(), Map.class);
        String method = (String) request.get("method");
        Object id = request.get("id");

        Object result = switch (method) {
            case "initialize" -> handleInitialize();
            case "tools/list" -> handleToolsList();
            case "tools/call" -> handleToolsCall(request, req);
            default -> null;
        };

        if (result == null) {
            resp.getWriter().write(mapper.writeValueAsString(Map.of(
                    "jsonrpc", "2.0",
                    "id", id != null ? id : 0,
                    "error", Map.of("code", -32601, "message", "Method not found: " + method)
            )));
            return;
        }

        resp.getWriter().write(mapper.writeValueAsString(Map.of(
                "jsonrpc", "2.0",
                "id", id != null ? id : 0,
                "result", result
        )));
    }

    private Map<String, Object> handleInitialize() {
        return Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of("tools", Map.of()),
                "serverInfo", Map.of("name", "carbonio-mailbox", "version", "1.0")
        );
    }

    private Map<String, Object> handleToolsList() {
        return Map.of("tools", toolRegistry.listTools());
    }

    @SuppressWarnings("unchecked")
    private Object handleToolsCall(Map<String, Object> request, HttpServletRequest httpReq) {
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        if (params == null) {
            return Map.of("content", List.of(
                    Map.of("type", "text", "text", "Error: missing params")));
        }
        String toolName = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());

        McpToolRegistry.ToolDefinition tool = toolRegistry.get(toolName);
        if (tool == null) {
            return Map.of("content", List.of(
                    Map.of("type", "text", "text", "Error: unknown tool: " + toolName)));
        }

        String callerUserId = httpReq.getHeader("X-Carbonio-User-Id");
        try {
            Object result = tool.handler().apply(arguments, callerUserId);
            String json = mapper.writeValueAsString(result);
            return Map.of("content", List.of(Map.of("type", "text", "text", json)));
        } catch (Exception e) {
            return Map.of("content", List.of(
                    Map.of("type", "text", "text", "Error: " + e.getMessage())));
        }
    }
}
