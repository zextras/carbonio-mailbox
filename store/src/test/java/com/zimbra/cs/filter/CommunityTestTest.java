// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


public class CommunityTestTest extends MailboxTestSuite {
    Account account;
    Mailbox mbox;

    @BeforeEach
    public void setUp() throws Exception {
        account = createAccount().create();
        RuleManager.clearCachedRules(account);
        mbox = MailboxManager.getInstance().getMailboxByAccount(account);

    }

    public void doRequest(String rule, String headerValue, String tag) throws Exception {

        account.setMailSieveScript("if " + rule + " { tag \"" + tag +"\"; }");
        List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                new ParsedMessage(("From: \"in.Telligent\" <noreply@in.telligent.com>\n"
                        + "X-Zimbra-Community-Notification-Type: "+headerValue+"\n").getBytes(), false),
                0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
        assertEquals(1, ids.size());
        Message msg = mbox.getMessageById(null, ids.get(0).getId());
        assertEquals(tag, ArrayUtil.getFirstElement(msg.getTags()));
    }

 @Test
 void testRequestNotifications() throws Exception {
  doRequest("community_requests", "bb196c30-fad3-4ad8-a644-2a0187fc5617", "request notifications");
 }

 @Test
 void testContentNotifications() throws Exception {
  doRequest("community_content", "6a3659db-dec2-477f-981c-ada53603ccbb", "content notifications");
 }

 @Test
 void testConnectionsNotifications() throws Exception {
  doRequest("community_connections", "194d3363-f5a8-43b4-a1bd-92a95f6dd76b", "connection notifications");
 }
}
