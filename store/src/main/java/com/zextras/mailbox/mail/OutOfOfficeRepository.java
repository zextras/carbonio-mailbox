// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.mail;

import com.zimbra.common.service.ServiceException;
import java.time.Instant;

public interface OutOfOfficeRepository {

  boolean alreadySent(int mailboxId, String sentTo, Instant since) throws ServiceException;

  void setSentTime(int mailboxId, String sentTo, Instant sentOn) throws ServiceException;

  void clear(int mailboxId) throws ServiceException;

  void prune(Instant before) throws ServiceException;
}
