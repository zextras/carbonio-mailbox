package com.zextras.mailbox.messageBroker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;

import io.vavr.control.Try;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;

class MessageBrokerFactoryTest {
	private static ClientAndServer consulServer;

	@BeforeAll
	public static void startUp() throws Exception {
		consulServer = startClientAndServer(8500);
	}

	@Test
	void shouldFailCreatingClient_WhenConsulTokenIsMissing() {
		Assertions.assertThrows(CreateMessageBrokerException.class,
				MessageBrokerFactory::getMessageBrokerClientInstance);
	}

	@Test
	void shouldCreateClient_WhenConsulTokenProvided() throws Exception {
		// Would avoid mock static, but we should refactor the code to avoid static methods
		MockedStatic<Files> mockFileSystem = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS);
		mockFileSystem.when(() -> Files.readString(any())).thenReturn("");
		consulServer
				.when(any())
				.respond(response().withStatusCode(200).withBody("[" +
						"{\"Value\": \"test\"}" +
						"]"));
		Assertions.assertDoesNotThrow(MessageBrokerFactory::getMessageBrokerClientInstance);
	}


}