package com.zextras.mailbox.client;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import org.mockserver.integration.ClientAndServer;

class FilesInstalledProviderTest {

	@Test
	void shouldThrowExceptionWhenConsulTokenFileDoesNotExist() {
		FilesInstalledProvider filesInstalledProvider = new FilesInstalledProvider(
				Paths.get("/wrongpath"));

		Assertions.assertThrows(UnableToCheckServiceInstalledException.class,
				filesInstalledProvider::isInstalled);
	}


	@Test
	void shouldThrowExceptionWhenNotAbleToContactConsul() throws Exception {
		var consulToken = FilesInstalledProviderTest.class.getResource("consulToken").toURI();

		FilesInstalledProvider filesInstalledProvider = new FilesInstalledProvider(
				Paths.get(consulToken));
		Assertions.assertThrows(UnableToCheckServiceInstalledException.class,
				filesInstalledProvider::isInstalled);
	}

	@Test
	void shouldReturnFilesAvailableWhenResponseFromConsulHasEmptyBody() throws Exception {
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
	void shouldReturnFilesNotAvailableWhenResponseFromConsulContainsEmptyArrayInBody() throws Exception {
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