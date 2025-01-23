/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.service.admin;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.config.enums.Service;
import com.zextras.mailbox.client.ServiceInstalledProvider;
import java.util.function.Supplier;

public class AdminServiceWithFilesInstalled extends AdminService {

	// This is something I would like to avoid, but CI does not allow to map container ports
	private static int messageBrokerPort = 20005;
	public static final String MESSAGE_BROKER_IMAGE = "rabbitmq:3.7.25-management-alpine";

	public static void setMessageBrokerPort(int port) {
		messageBrokerPort = port;
	}

	@Override
	protected ServiceInstalledProvider getFilesInstalledServiceProvider() {
		return () -> true;
	}

	@Override
	protected Supplier<MessageBrokerClient> getMessageBrokerClientProvider() {
		return () -> MessageBrokerClient.fromConfig("127.0.0.1", messageBrokerPort, "guest",
				"guest").withCurrentService(
				Service.MAILBOX);
	}
}
