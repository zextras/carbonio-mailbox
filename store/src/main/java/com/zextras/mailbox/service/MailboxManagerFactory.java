// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailboxManager;

/** Wraps {@link MailboxManager} to get instance and be able to provide it. */
public class MailboxManagerFactory {

  public MailboxManager getInstance() throws ServiceException {
    return MailboxManager.getInstance();
  }
}
