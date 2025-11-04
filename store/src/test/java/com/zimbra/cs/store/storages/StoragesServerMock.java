package com.zimbra.cs.store.storages;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;

public class StoragesServerMock {

	private final ClientAndServer storagesServer;

	public StoragesServerMock() {
		storagesServer = startClientAndServer(20010);
	}
	public void stop() {
		storagesServer.stop();
	}
	public String getUrl() {
		return "http://localhost:20010";
	}

	public void reset() {
		storagesServer.reset();
	}

	public void mockStoragesUpload(String content) {
		var jsonResponse = "{\n"
				+ "  \"query\": {\n"
				+ "    \"node\": \"node\",\n"
				+ "    \"version\": 1,\n"
				+ "    \"type\": \"type\"\n"
				+ "  },\n"
				+ "  \"resource\": \"filePath\",\n"
				+ "  \"digest\": \"digest\",\n"
				+ "  \"digest_algorithm\": \"SHA-256\",\n"
				+ "  \"size\": \"100\"\n"
				+ "}\n";
		storagesServer
				.when(request().withPath("/upload"))
				.respond(
						HttpResponse.response(jsonResponse)
								.withStatusCode(200));
		mockStoragesDownload(content);
	}
	public void mockStoragesDownload(String content) {
		storagesServer
				.when(request().withPath("/download"))
				.respond(
						HttpResponse.response()
								.withHeader("Content-Type", "application/octet-stream")
								.withBody(content.getBytes())
								.withStatusCode(200));
	}
}
