/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/mcp")
public class McpResource {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final McpToolRegistry toolRegistry;

    public McpResource(McpToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Response handle(
            Map<String, Object> request,
            @HeaderParam("X-Carbonio-User-Id") String callerUserId) {
        String method = (String) request.get("method");
        Object id = request.get("id");

        if (method == null) {
            return jsonRpcError(id, -32600, "Invalid Request: missing method");
        }

        Object result = switch (method) {
            case "initialize" -> handleInitialize();
            case "tools/list" -> handleToolsList();
            case "tools/call" -> handleToolsCall(request, callerUserId);
            default -> null;
        };

        if (result == null) {
            return jsonRpcError(id, -32601, "Method not found: " + method);
        }

        return Response.ok(Map.of("jsonrpc", "2.0", "id", id != null ? id : 0, "result", result))
                .header("Mcp-Session-Id", SESSION_ID)
                .build();
    }

    private Map<String, Object> handleInitialize() {
        return Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of("tools", Map.of()),
                "serverInfo", Map.of("name", "carbonio-mailbox", "version", "1.0"));
    }

    private Map<String, Object> handleToolsList() {
        return Map.of("tools", toolRegistry.listTools());
    }

    @SuppressWarnings("unchecked")
    private Object handleToolsCall(Map<String, Object> request, String callerUserId) {
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        if (params == null) {
            return errorContent("missing params");
        }
        String toolName = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());

        McpToolRegistry.ToolDefinition tool = toolRegistry.get(toolName);
        if (tool == null) {
            return errorContent("unknown tool: " + toolName);
        }

        if (callerUserId == null || callerUserId.isBlank()) {
            return errorContent("missing X-Carbonio-User-Id header");
        }

        try {
            Object toolResult = tool.handler().apply(arguments, callerUserId);
            String json = MAPPER.writeValueAsString(toolResult);
            return Map.of("content", List.of(Map.of("type", "text", "text", json)));
        } catch (Exception e) {
            return errorContent(e.getMessage());
        }
    }

    private static Map<String, Object> errorContent(String message) {
        return Map.of("content", List.of(Map.of("type", "text", "text", "Error: " + message)));
    }

    private Response jsonRpcError(Object id, int code, String message) {
        return Response.ok(Map.of(
                        "jsonrpc", "2.0",
                        "id", id != null ? id : 0,
                        "error", Map.of("code", code, "message", message)))
                .header("Mcp-Session-Id", SESSION_ID)
                .build();
    }
}
