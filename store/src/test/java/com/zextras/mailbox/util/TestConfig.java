/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import com.zimbra.common.localconfig.LC;

public class TestConfig {
	private static TestConfig singleton;

	private final String volumeDirectory;

	public TestConfig(String volumeDirectory) {
		this.volumeDirectory = volumeDirectory;
	}

	public String getVolumeDirectory() {
		return volumeDirectory;
	}

	public static TestConfig getInstance() {
		if (singleton == null) {
			singleton = new TestConfig(LC.zimbra_home.value() + "/build/test/");
		}
	return singleton;
	}

}
