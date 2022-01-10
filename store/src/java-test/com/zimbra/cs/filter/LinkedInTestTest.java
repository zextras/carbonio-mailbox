// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.jsieve.LinkedInTest;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;

/**
 * Unit test for {@link LinkedInTest}.
 *
 * @author ysasaki
 */
public final class LinkedInTestTest {

    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @Before
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

    @Test
    public void test() throws Exception {
        Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
        RuleManager.clearCachedRules(account);
        account.setMailSieveScript("if linkedin { tag \"linkedin\"; }");
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        // connections@linkedin.com
        List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                new ParsedMessage(("Sender: messages-noreply@bounce.linkedin.com\n" +
                        "From: LinkedIn Connections <connections@linkedin.com>").getBytes(), false),
                0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
        Assert.assertEquals(1, ids.size());
        Message msg = mbox.getMessageById(null, ids.get(0).getId());
        Assert.assertEquals("linkedin", ArrayUtil.getFirstElement(msg.getTags()));

        // deals
        ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                new ParsedMessage(("Sender: messages-noreply@bounce.linkedin.com\n" +
                    "From: Yuichi Sasaki via LinkedIn <member@linkedin.com>").getBytes(), false),
                0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
        Assert.assertEquals(1, ids.size());
        msg = mbox.getMessageById(null, ids.get(0).getId());
        Assert.assertEquals("linkedin", ArrayUtil.getFirstElement(msg.getTags()));
    }

}
