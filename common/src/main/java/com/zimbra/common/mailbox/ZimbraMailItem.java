// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import java.io.InputStream;

import com.zimbra.common.service.ServiceException;

public interface ZimbraMailItem extends BaseItemInfo {
    /** Returns the date the item's content was last modified as number of milliseconds since 1970-01-01 00:00:00 UTC.
     *  For immutable objects (e.g. received messages), this will be the same as the date the item was created. */
    public long getDate();
    /** Returns the item's size as it counts against mailbox quota.  For items
     *  that have a blob, this is the size in bytes of the raw blob. */
    public long getSize();
    public int getModifiedSequence();
    public InputStream getContentStream() throws ServiceException;
}
