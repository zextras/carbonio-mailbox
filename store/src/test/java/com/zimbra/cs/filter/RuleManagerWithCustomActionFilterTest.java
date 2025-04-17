// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.extension.ExtensionTestUtil;
import com.zimbra.cs.extension.ExtensionUtil;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.jsieve.SieveFactory;
import org.apache.jsieve.commands.AbstractActionCommand;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;


/**
 * Unit test for {@link com.zimbra.cs.filter.RuleManager}
 * used with CustomActionFilter extension
 */
public final class RuleManagerWithCustomActionFilterTest {

     public String testName;
    
    
    private static SieveFactory original_sf;
    private static Account account;
    

    @BeforeAll
    public static void init() throws Exception {

        // keep original sieve factory
        original_sf = RuleManager.getSieveFactory();

        ExtensionTestUtil.init();

        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());

        // make sure the behavior before registering custom actions
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        AbstractActionCommand ext =
                (AbstractActionCommand) ExtensionUtil.getExtension("discard");
        assertNull(ext);

        RuleManager.clearCachedRules(account);
        account.setMailSieveScript("if socialcast { discard; }");
        List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox, new ParsedMessage(
                "From: do-not-reply@socialcast.com\nReply-To: share@socialcast.com\nSubject: test".getBytes(), false),
                0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
        assertEquals(0, ids.size());

        // register custom action extensions
        ExtensionTestUtil.registerExtension("com.zimbra.extensions.DummyCustomDiscard");
        ExtensionTestUtil.registerExtension("com.zimbra.extensions.DummyCustomTag");
        ExtensionUtil.initAll();


    }

    @AfterAll
    public static void cleanUp() throws Exception{

        // set original ones
        JsieveConfigMapHandler.registerCommand("discard", "com.zimbra.cs.filter.jsieve.Discard");
        JsieveConfigMapHandler.registerCommand("tag", "com.zimbra.cs.filter.jsieve.Tag");

        // set original sieve factory back
        Method method = RuleManager.class.getDeclaredMethod("createSieveFactory");
        method.setAccessible(true);

        Field field =  RuleManager.class.getDeclaredField("SIEVE_FACTORY");
        field.setAccessible(true);

        field.set(RuleManager.class, original_sf);

        // inactivate custom action extension for just in case
        //ZimbraExtension discard_ext = ExtensionUtil.getExtension("discard");
        //discard_ext.destroy();

    }

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  System.out.println( testName);
 }

 @Test
 void tagAndCustomDiscard() throws Exception {

  // register custom action extension
  //ExtensionTestUtil.registerExtension("com.zimbra.extensions.DummyCustomDiscard");
  //ExtensionUtil.initAll();

  JsieveConfigMapHandler.registerCommand("discard", "com.zimbra.extensions.DummyCustomDiscard");
  JsieveConfigMapHandler.registerCommand("tag", "com.zimbra.cs.filter.jsieve.Tag");

  // recreate sieve factory
  Method method = RuleManager.class.getDeclaredMethod("createSieveFactory");
  method.setAccessible(true);

  Field field =  RuleManager.class.getDeclaredField("SIEVE_FACTORY");
  field.setAccessible(true);

  field.set(RuleManager.class, method.invoke(RuleManager.class));

  // make sure the registrations
  AbstractActionCommand ext =
    (AbstractActionCommand) ExtensionUtil.getExtension("discard");
  assertNotNull(ext);


  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  RuleManager.clearCachedRules(account);
  account.setMailSieveScript("if socialcast { tag \"socialcast\"; discard; }");
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox, new ParsedMessage(
      "From: do-not-reply@socialcast.com\nReply-To: share@socialcast.com\nSubject: test".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Inbox", mbox.getFolderById(null, msg.getFolderId()).getName());
  assertArrayEquals(new String[]{"socialcast", "priority"}, msg.getTags());

 }

 @Test
 void customDicardAndCustomTag() throws Exception {

  // register custom action extensions
  //ExtensionTestUtil.registerExtension("com.zimbra.extensions.DummyCustomDiscard");
  //ExtensionTestUtil.registerExtension("com.zimbra.extensions.DummyCustomTag");
  //ExtensionUtil.initAll();

  JsieveConfigMapHandler.registerCommand("discard", "com.zimbra.extensions.DummyCustomDiscard");
  JsieveConfigMapHandler.registerCommand("tag", "com.zimbra.extensions.DummyCustomTag");

  // recreate sieve factory
  Method method = RuleManager.class.getDeclaredMethod("createSieveFactory");
  method.setAccessible(true);

  Field field =  RuleManager.class.getDeclaredField("SIEVE_FACTORY");
  field.setAccessible(true);

  field.set(RuleManager.class, method.invoke(RuleManager.class));

  // make sure the registrations
  AbstractActionCommand discard_ext =
    (AbstractActionCommand) ExtensionUtil.getExtension("discard");
  assertNotNull(discard_ext);

  AbstractActionCommand tag_ext =
    (AbstractActionCommand) ExtensionUtil.getExtension("tag");
  assertNotNull(tag_ext);


  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  RuleManager.clearCachedRules(account);
  account.setMailSieveScript("if header :contains [\"Subject\"] [\"Zimbra\"] { tag \"socialcast\"; discard; }");
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox, new ParsedMessage(
      "From: do-not-reply@socialcast.com\nReply-To: share@socialcast.com\nSubject: Zimbra".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertArrayEquals(new String[]{"zimbra", "priority"}, msg.getTags());

  // inactivate custom tag action extension for just in case this test would be executed
  // before tagAndCustomDiscard test above
  //ZimbraExtension tag_ext2 = ExtensionUtil.getExtension("tag");
  //tag_ext2.destroy();

 }
    
    @AfterEach
    public void tearDown() {
        try {
            MailboxTestUtil.clearData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
