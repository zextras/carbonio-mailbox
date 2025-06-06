// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableMap;
import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ContactAutoComplete.AutoCompleteResult;
import com.zimbra.cs.mailbox.ContactAutoComplete.ContactEntry;
import com.zimbra.cs.mime.ParsedContact;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.mail.internet.InternetAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * Unit test for {@link ContactAutoComplete}.
 *
 * @author ysasaki
 */
class ContactAutoCompleteTest {

  private static Provisioning provisioning;

  @BeforeAll
  public static void init() throws Exception {
    System.setProperty("zimbra.config", "../store/src/test/resources/localconfig-test.xml");
    MailboxTestUtil.initServer();

    provisioning = Provisioning.getInstance();
  }

  @AfterEach
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void hitContact() throws Exception {
    ContactAutoComplete.AutoCompleteResult result = new ContactAutoComplete.AutoCompleteResult(10);
    final String accountName = UUID.randomUUID() + "@zimbra.com";
    final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
    result.rankings = new ContactRankings(account.getId());
    ContactAutoComplete.ContactEntry contact = new ContactAutoComplete.ContactEntry();
    contact.mDisplayName = "C1";
    contact.mEmail = "c1@zimbra.com";
    result.addEntry(contact);
    assertEquals(1, result.entries.size());

    contact = new ContactAutoComplete.ContactEntry();
    contact.mDisplayName = "C2";
    contact.mEmail = "c2@zimbra.com";
    result.addEntry(contact);
    assertEquals(2, result.entries.size());
  }



  @Test
  void lastNameFirstName() throws Exception {
    final String accountName = UUID.randomUUID() + "@zimbra.com";
    final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
    Map<String, Object> fields = new HashMap<>();
    fields.put(ContactConstants.A_firstName, "First");
    fields.put(ContactConstants.A_lastName, "Last");
    fields.put(ContactConstants.A_email, "test1@zimbra.com");
    mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);

    ContactAutoComplete autocomplete = new ContactAutoComplete(mbox.getAccount(),
        new OperationContext(mbox));
    assertEquals(1, autocomplete.query("first last", null, 100).entries.size());
    assertEquals(1, autocomplete.query("last first", null, 100).entries.size());
  }

  @Disabled
  public void spaceInFirstName() throws Exception {
    Account account = Provisioning.getInstance().getAccountByName("testContACEnv@zimbra.com");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
    Map<String, Object> fields = new HashMap<>();
    fields.put(ContactConstants.A_firstName, "First Second Third Forth");
    fields.put(ContactConstants.A_lastName, "Last");
    fields.put(ContactConstants.A_email, "test@zimbra.com");
    mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);

    ContactAutoComplete autocomplete = new ContactAutoComplete(mbox.getAccount(),
        new OperationContext(mbox));
    assertEquals(1, autocomplete.query("first second third forth", null, 100).entries.size());
  }

  @Test
  void reservedQueryTerm() throws Exception {
    final String accountName = UUID.randomUUID() + "@zimbra.com";
    final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
    Map<String, Object> fields = new HashMap<>();
    fields.put(ContactConstants.A_firstName, "not and or");
    fields.put(ContactConstants.A_lastName, "subject: from:");
    fields.put(ContactConstants.A_email, "test@zimbra.com");
    mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);

    Thread.sleep(500);
    ContactAutoComplete autocomplete = new ContactAutoComplete(mbox.getAccount(),
        new OperationContext(mbox));
    assertEquals(1, autocomplete.query("not", null, 100).entries.size());
    assertEquals(1, autocomplete.query("not and", null, 100).entries.size());
    assertEquals(1, autocomplete.query("not and or", null, 100).entries.size());
    assertEquals(1, autocomplete.query("subject:", null, 100).entries.size());
    assertEquals(1, autocomplete.query("subject: from:", null, 100).entries.size());
  }

  @Test
  void dash() throws Exception {
    final String accountName = UUID.randomUUID() + "@zimbra.com";
    final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
    Map<String, Object> fields = new HashMap<>();
    fields.put(ContactConstants.A_firstName, "Conf - Hillview");
    fields.put(ContactConstants.A_lastName, "test.server-vmware - dash");
    fields.put(ContactConstants.A_email, "test@zimbra.com");
    mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);

    Thread.sleep(500);
    ContactAutoComplete autocomplete = new ContactAutoComplete(mbox.getAccount(),
        new OperationContext(mbox));
    assertEquals(1, autocomplete.query("conf -", null, 100).entries.size());
    assertEquals(1, autocomplete.query("conf - h", null, 100).entries.size());
    assertEquals(1, autocomplete.query("test.server-vmware -", null, 100).entries.size());
    assertEquals(1, autocomplete.query("test.server-vmware - d", null, 100).entries.size());
  }

  @Test
  void hitGroup() throws Exception {
    ContactAutoComplete.AutoCompleteResult result = new ContactAutoComplete.AutoCompleteResult(10);
    final String accountName = UUID.randomUUID() + "@zimbra.com";
    final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
    result.rankings = new ContactRankings(account.getId());
    ContactAutoComplete.ContactEntry group = new ContactAutoComplete.ContactEntry();
    group.mDisplayName = "G1";
    group.mIsContactGroup = true;
    result.addEntry(group);

    assertEquals(1, result.entries.size());

    group = new ContactAutoComplete.ContactEntry();
    group.mDisplayName = "G2";
    group.mIsContactGroup = true;
    result.addEntry(group);

    assertEquals(2, result.entries.size());
  }

  @Test
  void addMatchedContacts() throws Exception {
    final String accountName = UUID.randomUUID() + "@zimbra.com";
    final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
    ContactAutoComplete comp = new ContactAutoComplete(account, null);
    ContactAutoComplete.AutoCompleteResult result = new ContactAutoComplete.AutoCompleteResult(10);
    result.rankings = new ContactRankings(account.getId());
    Map<String, Object> attrs = ImmutableMap.of(
        ContactConstants.A_firstName, "First",
        ContactConstants.A_middleName, "Middle",
        ContactConstants.A_lastName, "Last",
        ContactConstants.A_email, "first.last@zimbra.com");
    comp.addMatchedContacts("first f", attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);

    assertEquals(0, result.entries.size());
    result.clear();

    comp.addMatchedContacts("first mid", attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);

    assertEquals(1, result.entries.size());
    result.clear();

    comp.addMatchedContacts("first la", attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);

    assertEquals(1, result.entries.size());
    result.clear();

    comp.addMatchedContacts("middle last", attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);
    assertEquals(1, result.entries.size());
    result.clear();

    comp.addMatchedContacts("middle la", attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);
    assertEquals(1, result.entries.size());
    result.clear();

    comp.addMatchedContacts("ddle last", attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);
    assertEquals(0, result.entries.size());
    result.clear();

    comp.addMatchedContacts("first mid la", attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);
    assertEquals(1, result.entries.size());
    result.clear();

    attrs = ImmutableMap.of(
        ContactConstants.A_firstName, "Conf - hillview",
        ContactConstants.A_lastName, "test.server-vmware - dash",
        ContactConstants.A_email, "conf-hillview@zimbra.com");

    comp.addMatchedContacts("test.server-vmware -", attrs, Mailbox.ID_FOLDER_CONTACTS, null,
        result);
    assertEquals(1, result.entries.size());
    result.clear();

    comp.addMatchedContacts("test.server-vmware - d", attrs, Mailbox.ID_FOLDER_CONTACTS, null,
        result);
    assertEquals(1, result.entries.size());
    result.clear();

    comp.addMatchedContacts("conf - h", attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);
    assertEquals(1, result.entries.size());
    result.clear();
  }

  @Test
  void addMatchedContactsWithUnicodeCase() throws Exception {
    final String accountName = UUID.randomUUID() + "@zimbra.com";
    final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
    ContactAutoComplete comp = new ContactAutoComplete(account, null);
    ContactAutoComplete.AutoCompleteResult result = new ContactAutoComplete.AutoCompleteResult(10);
    result.rankings = new ContactRankings(account.getId());
    Map<String, Object> attrs = ImmutableMap.of(
        ContactConstants.A_firstName,
        "\u0421\u0440\u043f\u0441\u043a\u0438 \u0411\u043e\u0441\u043d\u0430 \u0438 \u0425\u0435\u0440\u0446\u0435\u0433\u043e\u0432\u0438\u043d\u0430",
        ContactConstants.A_lastName,
        "\u0441\u043a\u0438 \u0411\u043e\u0441\u043d\u0430 \u0438 \u0425\u0435\u0440\u0446\u0435\u0433\u043e\u0432\u0438\u043d\u0430",
        ContactConstants.A_email, "sr_BA@i18n.com");
    comp.addMatchedContacts(
        "\u0421\u0440\u043f\u0441\u043a\u0438 \u0411\u043e\u0441\u043d\u0430 \u0438 \u0425\u0435\u0440\u0446\u0435\u0433\u043e\u0432\u0438\u043d\u0430",
        attrs, Mailbox.ID_FOLDER_CONTACTS, null, result);

    assertEquals(1, result.entries.size());
    result.clear();
  }

  @Test
  void rankingTestContactWithSameEmailDifferentDisplayName() throws Exception {
    // Autocomplete should show same ranking for a email address present in difference contacts.
    final Account account = provisioning.createAccount(UUID.randomUUID() + "@zimbra.com", "secret", new HashMap<>());
    Mailbox mbox = MailboxManager.getInstance()
        .getMailboxByAccountId(account.getId());
    Map<String, Object> fields = new HashMap<>();
    fields.put(ContactConstants.A_firstName, "Pal");
    fields.put(ContactConstants.A_lastName, "One");
    fields.put(ContactConstants.A_email, "testauto@zimbra.com");
    mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);

    Map<String, Object> fields1 = new HashMap<>();
    fields1.put(ContactConstants.A_email, "testauto@zimbra.com");
    mbox.createContact(null, new ParsedContact(fields1), Mailbox.ID_FOLDER_CONTACTS, null);

    ContactRankings.increment(mbox.getAccountId(),
        Collections.singleton(new InternetAddress("testauto@zimbra.com")));
    ContactRankings.increment(mbox.getAccountId(),
        Collections.singleton(new InternetAddress("testauto@zimbra.com")));

    ContactAutoComplete autocomplete = new ContactAutoComplete(mbox.getAccount(),
        new OperationContext(mbox));
    AutoCompleteResult result = autocomplete.query("Pal", null, 10);
    assertEquals(1, result.entries.size());
    for (ContactEntry ce : result.entries) {
      assertEquals(2, ce.mRanking);
    }
    result.clear();

    result = autocomplete.query("testauto", null, 10);
    assertEquals(2, result.entries.size());
    for (ContactEntry ce : result.entries) {
      assertEquals(2, ce.mRanking);
    }
  }

  @Test
  void autocompleteTestNonExistingContact() throws Exception {
    final Account account = provisioning.createAccount(UUID.randomUUID() + "@zimbra.com", "secret", new HashMap<>());
    //AutoComplete should not return entry present in ranking table but contact does not exist.
    Mailbox mbox = MailboxManager.getInstance()
        .getMailboxByAccountId(account.getId());
    ContactRankings.increment(mbox.getAccountId(),
        Collections.singleton(new InternetAddress("noex@zimbra.com")));
    ContactRankings.increment(mbox.getAccountId(),
        Collections.singleton(new InternetAddress("noex@zimbra.com")));
    ContactAutoComplete autocomplete = new ContactAutoComplete(mbox.getAccount(),
        new OperationContext(mbox));
    assertEquals(0, autocomplete.query("noex", null, 10).entries.size());
  }
}
