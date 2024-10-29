package com.zextras.mailbox.messageBroker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zimbra.cs.httpclient.HttpClientFactory;
import io.vavr.control.Try;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

class MessageBrokerProviderTest {

	private ClientAndServer consulServer;
	private static final int CONSUL_PORT = 8500;

	private final HttpClientFactory httpClientFactoryMock = mock(HttpClientFactory.class);

	@BeforeEach
	public void startUp() throws Exception {
		when(httpClientFactoryMock.createWithProxy()).thenReturn(HttpClients.createMinimal());
		consulServer = startClientAndServer(CONSUL_PORT);
	}

	@Test
	void shouldFailCreatingClient_WhenConsulTokenIsMissing() {
		Assertions.assertThrows(CreateMessageBrokerException.class,
				() -> MessageBrokerProvider.getDefault().getMessageBrokerClientInstance());
	}

	@Test
	void shouldCreateClient_WhenConsulTokenProvided() {
		consulServer
				.when(any())
				.respond(response().withStatusCode(200).withBody("[" +
						"{\"Value\": \"test\"}" +
						"]"));
		Assertions.assertDoesNotThrow(() -> new MessageBrokerProvider(
				() -> Try.success("")).getMessageBrokerClientInstance());
	}

}