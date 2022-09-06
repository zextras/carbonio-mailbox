// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.client.event.ZModifyEvent;
import com.zimbra.common.service.ServiceException;

public interface ZSearchHit extends ToZJSONObject {
  String getId();

  String getSortField();

  void modifyNotification(ZModifyEvent event) throws ServiceException;
}
