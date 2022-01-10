// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;

public class ZModifyVoiceMailItemFolderEvent implements ZModifyItemFolderEvent {
	private String mFolderId;

	public ZModifyVoiceMailItemFolderEvent(String folderId) throws ServiceException {
		mFolderId = folderId;
	}

	public String getFolderId(String defaultValue) throws ServiceException {
		return mFolderId;
	}
}
