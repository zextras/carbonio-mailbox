package com.zextras.mailbox.messagebroker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;

import com.zextras.mailbox.util.PortUtil;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;

@Disabled
class MessageBrokerFactoryTest {
	// TODO: think about these tests. They rely on filesystem and other static things
	private static ClientAndServer consulServer;

	@BeforeAll
	public static void startUp() {
		consulServer = startClientAndServer(8500);
	}

	@Test
	void shouldFailCreatingClient_WhenConsulTokenIsMissing() {
		Assertions.assertThrows(CreateMessageBrokerException.class,
				MessageBrokerFactory::getMessageBrokerClientInstance);
	}

	@Test
	void shouldCreateClient_WhenConsulTokenProvided() {
		try (MockedStatic<Files> mockFileSystem = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
			mockFileSystem.when(() -> Files.readString(any())).thenReturn("");
			consulServer
					.when(any())
					.respond(response().withStatusCode(200).withBody("[" +
							"{\"Value\": \"test\"}" +
							"]"));
			Assertions.assertDoesNotThrow(MessageBrokerFactory::getMessageBrokerClientInstance);
		}
	}
}