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
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTest;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;

/**
 * Unit test for {@link ConversationTest}.
 *
 * @author ysasaki
 */
public final class ConversationTestTest {

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
    public void participated() throws Exception {
        Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
        RuleManager.clearCachedRules(account);
        account.setMailSieveScript("if conversation :where \"participated\" { tag \"participated\"; }");
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        mbox.addMessage(new OperationContext(mbox),
                new ParsedMessage("From: test1@zimbra.com\nSubject: test".getBytes(), false),
                MailboxTest.STANDARD_DELIVERY_OPTIONS, new DeliveryContext());

        List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                new ParsedMessage("From: test1@zimbra.com\nSubject: Re: test".getBytes(), false), 0, account.getName(),
                new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
        Assert.assertEquals(1, ids.size());
        Message msg = mbox.getMessageById(null, ids.get(0).getId());
        Assert.assertEquals(0, msg.getTags().length);

        DeliveryOptions dopt = new DeliveryOptions();
        dopt.setFolderId(Mailbox.ID_FOLDER_SENT);
        dopt.setFlags(Flag.BITMASK_FROM_ME);
        mbox.addMessage(new OperationContext(mbox),
                new ParsedMessage("From: test@zimbra.com\nSubject: Re: test".getBytes(), false),
                dopt, new DeliveryContext());

        ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                new ParsedMessage("From: test1@zimbra.com\nSubject: Re: test".getBytes(), false), 0, account.getName(),
                new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
        Assert.assertEquals(1, ids.size());
        msg = mbox.getMessageById(null, ids.get(0).getId());
        Assert.assertEquals("participated", ArrayUtil.getFirstElement(msg.getTags()));
    }

    @Test
    public void started() throws Exception {
        Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
        RuleManager.clearCachedRules(account);
        account.setMailSieveScript("if conversation :where \"started\" { tag \"started\"; }");
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        mbox.addMessage(new OperationContext(mbox),
                new ParsedMessage("From: test1@zimbra.com\nSubject: test".getBytes(), false),
                MailboxTest.STANDARD_DELIVERY_OPTIONS, new DeliveryContext());

        DeliveryOptions dopt = new DeliveryOptions();
        dopt.setFolderId(Mailbox.ID_FOLDER_SENT);
        dopt.setFlags(Flag.BITMASK_FROM_ME);
        mbox.addMessage(new OperationContext(mbox),
                new ParsedMessage("From: test@zimbra.com\nSubject: Re: test".getBytes(), false),
                dopt, new DeliveryContext());

        List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                new ParsedMessage("From: test1@zimbra.com\nSubject: Re: test".getBytes(), false), 0, account.getName(),
                new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
        Assert.assertEquals(1, ids.size());
        Message msg = mbox.getMessageById(null, ids.get(0).getId());
        Assert.assertEquals(0, msg.getTags().length);

        dopt = new DeliveryOptions();
        dopt.setFolderId(Mailbox.ID_FOLDER_SENT);
        dopt.setFlags(Flag.BITMASK_FROM_ME);
        mbox.addMessage(new OperationContext(mbox),
                new ParsedMessage("From: test@zimbra.com\nSubject: test1".getBytes(), false),
                dopt, new DeliveryContext());

        ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                new ParsedMessage("From: test1@zimbra.com\nSubject: Re: test1".getBytes(), false), 0, account.getName(),
                new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
        Assert.assertEquals(1, ids.size());
        msg = mbox.getMessageById(null, ids.get(0).getId());
        Assert.assertEquals("started", ArrayUtil.getFirstElement(msg.getTags()));
    }

}
