// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.Assert.fail;
import java.util.HashMap;
import java.util.List;

import org.apache.jsieve.SieveFactory;
import org.apache.jsieve.parser.generated.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.RuleManager.AdminFilterType;
import com.zimbra.cs.filter.RuleManager.FilterType;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;

public class RequireTest {
    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        Account account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @Before
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

    @Test
    public void requiresList() throws Exception {
        Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
        RuleManager.clearCachedRules(account);
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        RuleManager.clearCachedRules(account);
        account.unsetAdminSieveScriptBefore();
        account.unsetMailSieveScript();
        account.unsetAdminSieveScriptAfter();

        account.setAdminSieveScriptBefore("require [\"tag\", \"log\"]; require \"enotify\";");

        IncomingMessageHandler handler = new IncomingMessageHandler(
                new OperationContext(mbox), new DeliveryContext(),
                mbox, "test@zimbra.com",
                new ParsedMessage("From: test1@zimbra.com".getBytes(), false),
                0, Mailbox.ID_FOLDER_INBOX, true);
        ZimbraMailAdapter mailAdapter = new ZimbraMailAdapter(mbox, handler);

        String filter = RuleManager.getAdminScriptCacheKey(FilterType.INCOMING, AdminFilterType.BEFORE);
        Node node = RuleManager.getRulesNode(account, filter);
        SieveFactory SIEVE_FACTORY = RuleManager.getSieveFactory();
        SIEVE_FACTORY.evaluate(mailAdapter, node);

        List<String> requires = mailAdapter.getCapabilities();
        Assert.assertEquals(3, requires.size());
        Assert.assertEquals(requires.get(0), "tag");
        Assert.assertEquals(requires.get(1), "log");
        Assert.assertEquals(requires.get(2), "enotify");
    }

    @Test
    public void testRequireDeclaration() {
        try {
            Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
            RuleManager.clearCachedRules(account);
            Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
            // No "variable" require
            String filterScript = "set \"var\" \"hello\";\n"
                         + "if header :matches \"Subject\" \"*\" {\n"
                         + "  tag \"${var}\";\n"
                         + "}\n";
            account.setMailSieveScript(filterScript);
            String raw = "From: sender@zimbra.com\n"
                       + "To: test1@zimbra.com\n"
                       + "Subject: Test\n"
                       + "\n"
                       + "Hello World.";

            // 'require' control is mandatory
            account.setSieveRequireControlEnabled(true);
            List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                    new ParsedMessage(raw.getBytes(), false), 0, account.getName(), new DeliveryContext(),
                    Mailbox.ID_FOLDER_INBOX, true);
            Message msg = mbox.getMessageById(null, ids.get(0).getId());
            Assert.assertEquals(null, ArrayUtil.getFirstElement(msg.getTags()));

            // 'require' control is optional
            account.setSieveRequireControlEnabled(false);
            ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
                    new ParsedMessage(raw.getBytes(), false), 0, account.getName(), new DeliveryContext(),
                    Mailbox.ID_FOLDER_INBOX, true);
            msg = mbox.getMessageById(null, ids.get(0).getId());
            Assert.assertEquals("hello", ArrayUtil.getFirstElement(msg.getTags()));

        } catch (Exception e) {
            fail("No exception should be thrown: " + e);
        }
    }
}
