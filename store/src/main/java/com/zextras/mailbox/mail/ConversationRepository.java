// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.util.TypedIdList;
import java.util.List;

public interface ConversationRepository {

  void reparentChildren(MailItem oldParent, MailItem newParent) throws ServiceException;

  void openConversation(Mailbox mbox, String hash, int conversationId) throws ServiceException;

  void closeConversation(Mailbox mbox, String hash, int conversationId) throws ServiceException;

  void closeOldConversations(Mailbox mbox, int beforeDate) throws ServiceException;

  void changeOpenTargets(MailItem oldTarget, int newTargetId) throws ServiceException;

  UnderlyingData getByHash(Mailbox mbox, String hash) throws ServiceException;

  List<UnderlyingData> getByHashes(Mailbox mbox, List<String> hashes) throws ServiceException;

  TypedIdList listConvItems(Folder folder, long messageSyncStart, boolean descending, boolean older) throws ServiceException;
}
