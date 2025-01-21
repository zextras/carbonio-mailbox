package com.zextras.mailbox.client;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import org.mockserver.integration.ClientAndServer;

class FilesInstalledProviderTest {

	@Test
	void shouldThrowException_WhenConsulTokenFileDoesNotExist() {
		FilesInstalledProvider filesInstalledProvider = new FilesInstalledProvider(
				Paths.get("/wrongpath"));

		Assertions.assertThrows(RuntimeException.class,
				filesInstalledProvider::isInstalled);
	}

	@Test
	void shouldThrowException_WhenNotAbleToContactConsul() throws Exception {
		var consulToken = FilesInstalledProviderTest.class.getResource("consulToken").toURI();

		FilesInstalledProvider filesInstalledProvider = new FilesInstalledProvider(
				Paths.get(consulToken));
		Assertions.assertThrows(Exception.class,
				filesInstalledProvider::isInstalled);
	}

	@Test
	void shouldReturnFilesAvailable_WhenResponseFromConsulHasEmptyBody() throws Exception {
		try(ClientAndServer consulServer = startClientAndServer(8500)) {
			consulServer
					.when(request().withPath("/v1/health/checks/carbonio-files"))
					.respond(response().withStatusCode(200).withBody(""));

			var consulToken = FilesInstalledProviderTest.class.getResource("consulToken").toURI();
			FilesInstalledProvider filesInstalledProvider = new FilesInstalledProvider(
					Paths.get(consulToken));

			Assertions.assertTrue(filesInstalledProvider.isInstalled());
		}

	}

	@Test
	void shouldReturnFilesNotAvailable_WhenResponseFromConsulContainsEmptyArrayInBody() throws Exception {
		try(ClientAndServer consulServer = startClientAndServer(8500)) {
			consulServer
					.when(request().withPath("/v1/health/checks/carbonio-files"))
					.respond(response().withStatusCode(200).withBody("[]"));

			var consulToken = FilesInstalledProviderTest.class.getResource("consulToken").toURI();
			FilesInstalledProvider filesInstalledProvider = new FilesInstalledProvider(
					Paths.get(consulToken));

			Assertions.assertFalse(filesInstalledProvider.isInstalled());
		}
	}
}