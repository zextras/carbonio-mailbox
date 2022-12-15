// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import com.zimbra.common.service.ServiceException;

public interface ZimbraQueryHit {
    public int getItemId() throws ServiceException;
    public int getParentId() throws ServiceException;
    public int getModifiedSequence() throws ServiceException;
    public MailItemType getMailItemType() throws ServiceException;
    public int getImapUid() throws ServiceException;
    public int getFlagBitmask() throws ServiceException;
    public String[] getTags() throws ServiceException;
}
