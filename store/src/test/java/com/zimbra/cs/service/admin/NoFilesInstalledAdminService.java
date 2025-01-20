/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.service.admin;

import com.zextras.mailbox.client.ServiceInstalledProvider;

public class NoFilesInstalledAdminService extends AdminServiceWithFakeBrokerClient {

	private static class FilesNotInstalledProvider implements ServiceInstalledProvider {

		@Override
		public boolean isInstalled() {
			return false;
		}
	}

	@Override
	protected ServiceInstalledProvider getFilesInstalledServiceProvider() {
		return new FilesNotInstalledProvider();
	}
}
