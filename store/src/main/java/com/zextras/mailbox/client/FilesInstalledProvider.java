/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.client;

import java.nio.file.Files;
import java.nio.file.Path;

public class FilesInstalledProvider implements ServiceInstalledProvider {

	private final Path consulTokenFilePath;

	public FilesInstalledProvider(Path consulTokenFilePath) {
		this.consulTokenFilePath = consulTokenFilePath;
	}

	public boolean isInstalled() throws UnableToCheckServiceInstalledException {
		try {
			final String consulToken = Files.readString(consulTokenFilePath);
			ServiceDiscoverHttpClient serviceDiscoverHttpClient = ServiceDiscoverHttpClient.defaultUrl()
					.withToken(consulToken);
			return serviceDiscoverHttpClient.isServiceInstalled("carbonio-files").get();
		} catch (Exception e) {
			throw new UnableToCheckServiceInstalledException(e.getMessage());
		}
	}

}
