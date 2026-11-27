// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.util.TypedIdList;

public interface TombstoneRepository {

  void writeTombstones(Mailbox mbox, TypedIdList tombstones) throws ServiceException;

  TypedIdList readTombstones(Mailbox mbox, long lastSync, boolean equalModSeq) throws ServiceException;

  int purgeTombstones(Mailbox mbox, int beforeDate) throws ServiceException;
}
