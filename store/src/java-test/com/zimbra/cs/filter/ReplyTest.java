// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.mail.SendMsgTest.DirectInsertionMailboxManager;
import com.zimbra.cs.service.util.ItemId;

/**
 * @author zimbra
 *
 */
public class ReplyTest {

    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        MailboxTestUtil.clearData();
        Provisioning prov = Provisioning.getInstance();

        Map<String, Object> attrs = Maps.newHashMap();
        prov.createDomain("zimbra.com", attrs);

        attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        attrs.put(Provisioning.A_zimbraSieveNotifyActionRFCCompliant, "FALSE");
        attrs.put(Provisioning.A_zimbraSieveRequireControlEnabled, "TRUE");
        prov.createAccount("test@zimbra.com", "secret", attrs);

        attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        attrs.put(Provisioning.A_zimbraSieveNotifyActionRFCCompliant, "FALSE");
        prov.createAccount("test2@zimbra.com", "secret", attrs);

        // this MailboxManager does everything except actually send mail
        MailboxManager.setInstance(new DirectInsertionMailboxManager());
    }

    @Before
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

    @Test
    public void testReply() {
        try {

            String sampleMsg = "from: test2@zimbra.com\n" + "Return-Path: test2@zimbra.com\n"
                + "Subject: Hello\n" + "to: test@zimbra.com\n";

            Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
            Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "test2@zimbra.com");

            Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
            Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);

            RuleManager.clearCachedRules(acct1);
            String filterScript = "if anyof (true) { reply \"Hello World\"" + "    stop;" + "}";
            acct1.setMailSieveScript(filterScript);
            List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox1),
                mbox1, new ParsedMessage(sampleMsg.getBytes(), false), 0, acct1.getName(),
                new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
            Assert.assertEquals(1, ids.size());
            Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
                .getIds(MailItem.Type.MESSAGE).get(0);
            Message notifyMsg = mbox2.getMessageById(null, item);
            Assert.assertEquals("Hello World", notifyMsg.getFragment());
        } catch (Exception e) {
            fail("No exception should be thrown");
        }

    }

    @Test
    public void testReplyMimeVariables() {
        try {
            String sampleMsg = "from: test2@zimbra.com\n" + "Return-Path: test2@zimbra.com\n"
                + "Subject: Hello\n" + "to: test@zimbra.com\n";

            Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
            Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "test2@zimbra.com");

            Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
            Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);

            RuleManager.clearCachedRules(acct1);
            String filterScript = "require \"variables\";\n"
                + "set \"var\" \"World\";\n"
                + "if anyof (true) { reply \"${Subject} ${var}\"" + "    stop;" + "}";
            acct1.setMailSieveScript(filterScript);
            List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox1),
                mbox1, new ParsedMessage(sampleMsg.getBytes(), false), 0, acct1.getName(),
                new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
            Assert.assertEquals(1, ids.size());
            Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
                .getIds(MailItem.Type.MESSAGE).get(0);
            Message notifyMsg = mbox2.getMessageById(null, item);
            Assert.assertEquals("Hello World", notifyMsg.getFragment());
        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }
}
