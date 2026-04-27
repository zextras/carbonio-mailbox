/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zextras.mailbox.api.InternalApiContextHandler;
import com.zextras.mailbox.api.rest.resource.dto.AccountSearchResponse;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.api.rest.service.MailboxService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import io.vavr.control.Try;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import java.util.Map;

public class McpContextHandler {

    private McpContextHandler() {}

    public static ServletContextHandler create() {
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/mcp");
        context.setVirtualHosts(new String[]{"@" + InternalApiContextHandler.CONNECTOR_NAME});

        McpToolRegistry registry = new McpToolRegistry();
        AccountService accountService = createAccountService();
        registerSearchAccountsTool(registry, accountService);

        context.addServlet(new ServletHolder(new McpServlet(registry)), "/*");
        return context;
    }

    private static AccountService createAccountService() {
        final MailboxService mailboxService = new MailboxService(
                Provisioning::getInstance,
                () -> {
                    try {
                        return MailboxManager.getInstance();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    try {
                        return SoapProvisioning.getAdminInstance();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                });
        return new AccountService(Provisioning::getInstance, mailboxService);
    }

    private static void registerSearchAccountsTool(McpToolRegistry registry, AccountService accountService) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = mapper.createObjectNode();
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

        registry.register(new McpToolRegistry.ToolDefinition(
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
                }
        ));
    }
}
