// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.common.mailbox.ContactConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;

/**
 * Unit test for {@link AddressBookTest}.
 *
 * @author ysasaki
 */
public final class AddressBookTestTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void filter() throws Exception {
  Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
  RuleManager.clearCachedRules(account);
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
  mbox.createContact(null, new ParsedContact(Collections.<String, Object>singletonMap(
    ContactConstants.A_email, "test1@zimbra.com")), Mailbox.ID_FOLDER_CONTACTS, null);

  account.setMailSieveScript("if addressbook :in \"From\" { tag \"Priority\"; }");
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("From: test1@zimbra.com".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
 }

}
