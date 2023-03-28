//// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
////
//// SPDX-License-Identifier: GPL-2.0-only
//
//package com.zimbra.cs.mailbox;
//
//import static org.junit.Assert.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyBoolean;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.mockStatic;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.zimbra.common.account.Key;
//import com.zimbra.common.mailbox.ContactConstants;
//import com.zimbra.common.mime.InternetAddress;
//import com.zimbra.common.service.ServiceException;
//import com.zimbra.common.soap.Element;
//import com.zimbra.common.soap.MailConstants;
//import com.zimbra.cs.account.Account;
//import com.zimbra.cs.account.Provisioning;
//import com.zimbra.cs.db.DbMailItem;
//import com.zimbra.cs.db.DbPool;
//import com.zimbra.cs.db.DbPool.DbConnection;
//import com.zimbra.cs.db.DbResults;
//import com.zimbra.cs.db.DbUtil;
//import com.zimbra.cs.gal.GalGroup.GroupInfo;
//import com.zimbra.cs.gal.GalGroupInfoProvider;
//import com.zimbra.cs.mailbox.Contact.Attachment;
//import com.zimbra.cs.mime.Mime;
//import com.zimbra.cs.mime.ParsedContact;
//import com.zimbra.cs.service.formatter.ArchiveFormatter;
//import com.zimbra.cs.service.formatter.ArchiveFormatter.ArchiveInputEntry;
//import com.zimbra.cs.service.formatter.ArchiveFormatter.ArchiveInputStream;
//import com.zimbra.cs.service.formatter.TarArchiveInputStream;
//import com.zimbra.cs.service.formatter.VCard;
//import com.zimbra.cs.service.mail.ToXML;
//import com.zimbra.cs.service.util.ItemIdFormatter;
//import com.zimbra.cs.util.JMSession;
//import com.zimbra.cs.util.ZTestWatchman;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.zip.GZIPInputStream;
//import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimePart;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.MethodRule;
//import org.junit.rules.TestName;
//import org.mockito.MockedStatic;
//
///**
// * Unit test for {@link Contact}.
// *
// * @author ysasaki
// */
//public final class ContactTest {
//
//  @Rule public TestName testName = new TestName();
//  @Rule public MethodRule watchman = new ZTestWatchman();
//
//  @BeforeClass
//  public static void init() throws Exception {
//    MailboxTestUtil.initServer();
//  }
//
//  @Before
//  public void setUp() throws Exception {
//    System.out.println(testName.getMethodName());
//    Provisioning prov = Provisioning.getInstance();
//    prov.createAccount("testCont@zimbra.com", "secret", new HashMap<String, Object>());
//    prov.createAccount("test6232@zimbra.com", "secret", new HashMap<String, Object>());
//  }
//
//  @Test
//  public void reanalyze() throws Exception {
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    Map<String, Object> fields = new HashMap<String, Object>();
//    fields.put(ContactConstants.A_firstName, "First1");
//    fields.put(ContactConstants.A_lastName, "Last1");
//    Contact contact =
//        mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);
//
//    DbConnection conn = DbPool.getConnection(mbox);
//
//    assertEquals(
//        "Last1, First1",
//        DbUtil.executeQuery(
//                conn,
//                "SELECT sender FROM mboxgroup1.mail_item WHERE mailbox_id = ? AND id = ?",
//                mbox.getId(),
//                contact.getId())
//            .getString(1));
//
//    fields.put(ContactConstants.A_firstName, "First2");
//    fields.put(ContactConstants.A_lastName, "Last2");
//    mbox.modifyContact(null, contact.getId(), new ParsedContact(fields));
//
//    assertEquals(
//        "Last2, First2",
//        DbUtil.executeQuery(
//                conn,
//                "SELECT sender FROM mboxgroup1.mail_item WHERE mailbox_id = ? AND id = ?",
//                mbox.getId(),
//                contact.getId())
//            .getString(1));
//
//    conn.closeQuietly();
//  }
//
//  @Test
//  public void tooLongSender() throws Exception {
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    Map<String, Object> fields = new HashMap<String, Object>();
//    fields.put(ContactConstants.A_firstName, Strings.repeat("F", 129));
//    Contact contact =
//        mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);
//
//    DbConnection conn = DbPool.getConnection(mbox);
//
//    assertEquals(
//        Strings.repeat("F", 128),
//        DbUtil.executeQuery(
//                conn,
//                "SELECT sender FROM mboxgroup1.mail_item WHERE mailbox_id = ? AND id = ?",
//                mbox.getId(),
//                contact.getId())
//            .getString(1));
//
//    fields.put(ContactConstants.A_firstName, null);
//    fields.put(ContactConstants.A_lastName, Strings.repeat("L", 129));
//    mbox.modifyContact(null, contact.getId(), new ParsedContact(fields));
//
//    assertEquals(
//        Strings.repeat("L", 128),
//        DbUtil.executeQuery(
//                conn,
//                "SELECT sender FROM mboxgroup1.mail_item WHERE mailbox_id = ? AND id = ?",
//                mbox.getId(),
//                contact.getId())
//            .getString(1));
//
//    conn.closeQuietly();
//  }
//
//  /**
//   * Bug 77746 Test that VCARD formatting escapes ';' and ',' chars which are part of name
//   * components
//   */
//  @Test
//  public void semiColonAndCommaInName() throws Exception {
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    Map<String, Object> fields = new HashMap<String, Object>();
//    fields.put(ContactConstants.A_lastName, "Last");
//    fields.put(ContactConstants.A_firstName, "First ; SemiColon");
//    fields.put(ContactConstants.A_middleName, "Middle , Comma");
//    fields.put(ContactConstants.A_namePrefix, "Ms.");
//    Contact contact =
//        mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);
//
//    VCard vcard = VCard.formatContact(contact);
//    String vcardAsString = vcard.getFormatted();
//    String expectedPattern = "N:Last;First \\; SemiColon;Middle \\, Comma;Ms.;";
//    String assertMsg =
//        String.format("Vcard\n%s\nshould contain string [%s]", vcardAsString, expectedPattern);
//    assertTrue(assertMsg, vcardAsString.contains(expectedPattern));
//  }
//
//  @Test
//  public void existsInContacts() throws Exception {
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    mbox.createContact(
//        null,
//        new ParsedContact(Collections.singletonMap(ContactConstants.A_email, "test1@zimbra.com")),
//        Mailbox.ID_FOLDER_CONTACTS,
//        null);
//    MailboxTestUtil.index(mbox);
//    Thread.sleep(500);
//    assertTrue(
//        mbox.index.existsInContacts(
//            ImmutableList.of(
//                new InternetAddress("Test <test1@zimbra.com>"),
//                new InternetAddress("Test <test2@zimbra.com>"))));
//    assertFalse(
//        mbox.index.existsInContacts(
//            ImmutableList.of(
//                new InternetAddress("Test <test2@zimbra.com>"),
//                new InternetAddress("Test <test3@zimbra.com>"))));
//  }
//
//  @Test
//  public void createAutoContact() throws Exception {
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    List<Contact> contacts =
//        mbox.createAutoContact(
//            null,
//            ImmutableList.of(
//                new InternetAddress("Test 1", "TEST1@zimbra.com"),
//                new InternetAddress("Test 2", "TEST2@zimbra.com")));
//
//    assertEquals(2, contacts.size());
//    assertEquals("1, Test", contacts.get(0).getFileAsString());
//    assertEquals("TEST1@zimbra.com", contacts.get(0).getFields().get(ContactConstants.A_email));
//    assertEquals("2, Test", contacts.get(1).getFileAsString());
//    assertEquals("TEST2@zimbra.com", contacts.get(1).getFields().get(ContactConstants.A_email));
//
//    Collection<javax.mail.Address> newAddrs =
//        mbox.newContactAddrs(
//            ImmutableList.of(
//                (javax.mail.Address)
//                    new javax.mail.internet.InternetAddress("test1@zimbra.com", "Test 1"),
//                (javax.mail.Address)
//                    new javax.mail.internet.InternetAddress("test2@zimbra.com", "Test 2")),
//            "aaa");
//
//    assertEquals(0, newAddrs.size());
//  }
//
//  /** Confirms that locator is not set for contacts. */
//  @Test
//  public void locator() throws Exception {
//    // Create contact.
//    Map<String, String> attrs = Maps.newHashMap();
//    attrs.put(ContactConstants.A_fullName, "Volume Id");
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    mbox.createContact(null, new ParsedContact(attrs), Mailbox.ID_FOLDER_CONTACTS, null);
//
//    // Check volume id in database.
//    String sql =
//        String.format(
//            "SELECT COUNT(*) FROM %s WHERE type = %d AND blob_digest IS NULL AND locator IS NOT"
//                + " NULL",
//            DbMailItem.getMailItemTableName(mbox), MailItem.Type.CONTACT.toByte());
//    DbResults results = DbUtil.executeQuery(sql);
//    assertEquals("Found non-null locator values for contacts", 0, results.getInt(1));
//  }
//
//  /** Tests {@link Attachment#getContent()} (bug 36974). */
//  @Test
//  public void getAttachmentContent() throws Exception {
//    // Create a contact with an attachment.
//    Map<String, String> attrs = new HashMap<String, String>();
//    attrs.put("fullName", "Get Attachment Content");
//    byte[] attachData = "attachment 1".getBytes();
//    Attachment textAttachment = new Attachment(attachData, "text/plain", "customField", "text.txt");
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//
//    mbox.createContact(
//        null,
//        new ParsedContact(attrs, Lists.newArrayList(textAttachment)),
//        Mailbox.ID_FOLDER_CONTACTS,
//        null);
//
//    // Call getContent() on all attachments.
//    for (Contact contact : mbox.getContactList(null, Mailbox.ID_FOLDER_CONTACTS)) {
//      List<Attachment> attachments = contact.getAttachments();
//      for (Attachment attach : attachments) {
//        attach.getContent();
//      }
//    }
//  }
//
//  /** Modify Contact having an attachment (bug 70488). */
//  @Test
//  public void modifyContactHavingAttachment() throws Exception {
//    // Create a contact with an attachment.
//    Map<String, String> attrs = new HashMap<String, String>();
//    attrs.put("fullName", "Contact Initial Content");
//    byte[] attachData = "attachment 1".getBytes();
//    Attachment textAttachment = new Attachment(attachData, "text/plain", "customField", "file.txt");
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    Contact contact =
//        mbox.createContact(
//            null,
//            new ParsedContact(attrs, Lists.newArrayList(textAttachment)),
//            Mailbox.ID_FOLDER_CONTACTS,
//            null);
//
//    ParsedContact pc =
//        new ParsedContact(contact)
//            .modify(new ParsedContact.FieldDeltaList(), new ArrayList<Attachment>(), "ownerId");
//    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession(), pc.getContentStream());
//    MimePart mp = Mime.getMimePart(mm, "1");
//    assertEquals("text/plain", mp.getContentType());
//    assertEquals("attachment 1", mp.getContent());
//  }
//
//  /** Tests Invalid image attachment (bug 71868). */
//  @Test
//  public void createInvalidImageAttachment() throws Exception {
//    // Create a contact with an attachment.
//    Map<String, String> attrs = new HashMap<String, String>();
//    attrs.put("fullName", "Get Attachment Content");
//    byte[] attachData = "attachment 1".getBytes();
//    Attachment attachment = new Attachment(attachData, "image/png", "image", "file1.png");
//    try {
//      ParsedContact pc = new ParsedContact(attrs, Lists.newArrayList(attachment));
//      fail("Expected INVALID_IMAGE exception");
//    } catch (ServiceException se) {
//      assertEquals("check the INVALID_IMAGE exception", "mail.INVALID_IMAGE", se.getCode());
//    }
//  }
//
//  /** Tests Invalid image attachment (bug 71868). */
//  @Test
//  public void modifyInvalidImageAttachment() throws Exception {
//    // Create a contact with an attachment.
//    Map<String, String> attrs = new HashMap<String, String>();
//    attrs.put("fullName", "Contact Initial Content");
//    byte[] attachData = "attachment 1".getBytes();
//    Attachment attachment1 = new Attachment(attachData, "image/png", "customField", "image.png");
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    Contact contact =
//        mbox.createContact(
//            null,
//            new ParsedContact(attrs, Lists.newArrayList(attachment1)),
//            Mailbox.ID_FOLDER_CONTACTS,
//            null);
//    Attachment attachment2 = new Attachment(attachData, "image/png", "image", "image2.png");
//    try {
//      ParsedContact pc =
//          new ParsedContact(contact)
//              .modify(
//                  new ParsedContact.FieldDeltaList(), Lists.newArrayList(attachment2), "ownerId");
//    } catch (ServiceException se) {
//      assertEquals("check the INVALID_IMAGE exception", "mail.INVALID_IMAGE", se.getCode());
//    }
//  }
//
//  @Test
//  public void testEncodeContact() throws Exception {
//    Account account = Provisioning.getInstance().getAccountByName("testCont@zimbra.com");
//    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
//    Map<String, Object> fields = new HashMap<String, Object>();
//    fields.put(ContactConstants.A_userCertificate, "{\"ZMVAL\":[\"Cert1149638887753217\"]}");
//    Contact contact =
//        mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);
//
//    Element response = new Element.XMLElement(MailConstants.MODIFY_CONTACT_RESPONSE);
//    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "testCont@zimbra.com");
//    ToXML.encodeContact(
//        response, new ItemIdFormatter(), new OperationContext(acct), contact, true, null);
//    assertEquals(response.getElement("cn").getElement("a").getText(), "Cert1149638887753217");
//  }
//
//  @Test
//  public void testZCS6232() throws Exception {
//    Account account = Provisioning.getInstance().getAccountByName("test6232@zimbra.com");
//    // mocking the group not to have view permission
//    try (MockedStatic<ToXML> toXMLMockedStatic = mockStatic(ToXML.class)) {
//      GalGroupInfoProvider galGroupInfoProvider = mock(GalGroupInfoProvider.class);
//      doReturn(GroupInfo.IS_GROUP)
//          .when(galGroupInfoProvider)
//          .getGroupInfo(anyString(), anyBoolean(), any(Account.class), any(Account.class));
//      toXMLMockedStatic.when(ToXML::getGalGroupInfoProvider).thenReturn(galGroupInfoProvider);
//      toXMLMockedStatic
//          .when(() -> ToXML.hasDLViewRight("mydl@zimbra.com", account, account))
//          .thenCallRealMethod();
//      assertFalse(ToXML.hasDLViewRight("mydl@zimbra.com", account, account));
//    }
//  }
//
//  @Test
//  public void testZCS6232WithNullEmail() throws Exception {
//    Account account = Provisioning.getInstance().getAccountByName("test6232@zimbra.com");
//    GalGroupInfoProvider galGroupInfoProvider = mock(GalGroupInfoProvider.class);
//    // inside try logic to avoid Mockito exception when mocking static same class (Thread scope)
//    try (MockedStatic<ToXML> toXMLMockedStatic = mockStatic(ToXML.class)) {
//      doReturn(GroupInfo.IS_GROUP)
//          .when(galGroupInfoProvider)
//          .getGroupInfo(anyString(), anyBoolean(), any(Account.class), any(Account.class));
//      toXMLMockedStatic.when(ToXML::getGalGroupInfoProvider).thenReturn(galGroupInfoProvider);
//      toXMLMockedStatic
//          .when(() -> ToXML.hasDLViewRight(null, account, account))
//          .thenCallRealMethod();
//      assertTrue(ToXML.hasDLViewRight(null, account, account));
//    }
//  }
//
//  @Test
//  public void testTruncatedContactsTgzImport() throws IOException {
//    File file = new File(MailboxTestUtil.getZimbraServerDir("") + "src/test/resources/Truncated.tgz");
//    System.out.println(file.getAbsolutePath());
//    InputStream is = new FileInputStream(file);
//    ArchiveInputStream ais = new TarArchiveInputStream(new GZIPInputStream(is), "UTF-8");
//    ArchiveInputEntry aie;
//    boolean errorCaught = false;
//    while ((aie = ais.getNextEntry()) != null) {
//      try {
//        ArchiveFormatter.readArchiveEntry(ais, aie);
//      } catch (IOException e) {
//        e.printStackTrace();
//        errorCaught = true;
//        break;
//      }
//    }
//    assertTrue(errorCaught);
//  }
//
//  @After
//  public void tearDown() {
//    try {
//      MailboxTestUtil.clearData();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//}
