/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralLocation;
import com.zimbra.cs.ephemeral.EphemeralResult;
import com.zimbra.cs.ephemeral.EphemeralStore;

public class SSDBEphemeralStore extends EphemeralStore {

	@Override
	public EphemeralResult get(EphemeralKey key, EphemeralLocation location) throws ServiceException {
		return null;
	}

	@Override
	public void set(EphemeralInput attribute, EphemeralLocation location) throws ServiceException {

	}

	@Override
	public void update(EphemeralInput attribute, EphemeralLocation location) throws ServiceException {

	}

	@Override
	public void delete(EphemeralKey key, String value, EphemeralLocation location)
			throws ServiceException {

	}

	@Override
	public boolean has(EphemeralKey key, EphemeralLocation location) throws ServiceException {
		return false;
	}

	@Override
	public void purgeExpired(EphemeralKey key, EphemeralLocation location) throws ServiceException {

	}

	@Override
	public void deleteData(EphemeralLocation location) throws ServiceException {

	}
}
