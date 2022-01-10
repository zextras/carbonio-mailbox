// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import java.io.Closeable;

import com.zimbra.common.service.ServiceException;

public interface ZimbraQueryHitResults extends Closeable {
    public ZimbraQueryHit getNext() throws ServiceException;
}
