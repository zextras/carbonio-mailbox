/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesInstalledProvider implements ServiceInstalledProvider {

	private final Path consulTokenFilePath;
	private final String serviceDiscoverURL;

	public FilesInstalledProvider(Path consulTokenFilePath, String serviceeDiscoverURL) {
		this.consulTokenFilePath = consulTokenFilePath;
    this.serviceDiscoverURL = serviceeDiscoverURL;
  }

	public boolean isInstalled() {
		final String consulToken;
		try {
			consulToken = Files.readString(consulTokenFilePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ServiceDiscoverHttpClient serviceDiscoverHttpClient = new ServiceDiscoverHttpClient(serviceDiscoverURL);
			return serviceDiscoverHttpClient.isServiceInstalled("carbonio-files").get();
	}

}
