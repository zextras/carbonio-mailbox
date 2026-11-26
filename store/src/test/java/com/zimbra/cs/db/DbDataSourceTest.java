// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import org.junit.jupiter.api.Test;

class DbDataSourceTest extends MailboxTestSuite {

  private static final String HASH = "hash-of-conversation-0000001";

  @Test
  void moveToPurgedConversationsStoresDataSourceIdAsGiven() throws Exception {
    Account account = createAccount().create();
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
    MailItem conversation = createItem(mbox);
    openConversation(mbox, conversation.getId());
    String dataSourceId = "data-source-with-a-'-quote";

    DbDataSource.moveToPurgedConversations(mbox, conversation, dataSourceId);

    DbResults rows =
        DbTestUtil.executeQuery(
            "SELECT data_source_id, item_id, hash FROM "
                + DbDataSource.getPurgedConvsTableName(mbox)
                + " WHERE mailbox_id = ?",
            mbox.getId());
    assertEquals(1, rows.size());
    rows.next();
    assertEquals(dataSourceId, rows.getString("data_source_id"));
    assertEquals(conversation.getId(), rows.getInt("item_id"));
    assertEquals(HASH, rows.getString("hash"));
  }

  private static MailItem createItem(Mailbox mbox) throws Exception {
    return mbox.createFolder(
        null, "/purge-test", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  }

  private static void openConversation(Mailbox mbox, int convId) throws Exception {
    DbTestUtil.executeUpdate(
        "INSERT INTO "
            + DbMailItem.getConversationTableName(mbox.getId(), mbox.getSchemaGroupId())
            + " (mailbox_id, hash, conv_id) VALUES (?, ?, ?)",
        mbox.getId(),
        HASH,
        convId);
  }
}
