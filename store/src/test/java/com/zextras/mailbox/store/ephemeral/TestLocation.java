/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.zimbra.cs.ephemeral.EphemeralLocation;

class TestLocation extends EphemeralLocation {

	private final String[] location;

	TestLocation(String[] location) {
		this.location = location;
	}

	@Override
	public String[] getLocation() {
		return this.location;
	}
}
