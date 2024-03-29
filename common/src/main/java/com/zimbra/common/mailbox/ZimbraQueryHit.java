// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import com.zimbra.common.service.ServiceException;

public interface ZimbraQueryHit {
    int getItemId() throws ServiceException;
    int getParentId() throws ServiceException;
    int getModifiedSequence() throws ServiceException;
    MailItemType getMailItemType() throws ServiceException;
    int getImapUid() throws ServiceException;
    int getFlagBitmask() throws ServiceException;
    String[] getTags() throws ServiceException;
}
