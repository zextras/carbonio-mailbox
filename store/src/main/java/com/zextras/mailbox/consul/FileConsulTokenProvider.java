package com.zextras.mailbox.consul;

import io.vavr.control.Try;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileConsulTokenProvider implements
		ConsulTokenProvider {

	private final Path path;

	public FileConsulTokenProvider(Path path) {
		this.path = path;
	}

	@Override
	public Try<String> getToken() {
		return Try.of(() -> Files.readString(path));
	}
}
