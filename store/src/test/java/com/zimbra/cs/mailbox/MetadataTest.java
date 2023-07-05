// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.ibm.icu.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.GregorianCalendar;
import com.zimbra.common.calendar.ParsedDateTime;
import com.zimbra.common.calendar.TimeZoneMap;
import com.zimbra.common.calendar.WellKnownTimeZones;
import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.mime.shim.JavaMailMimeMessage;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.zmime.ZSharedFileInputStream;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox.SetCalendarItemData;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.util.JMSession;

@Disabled("failing in hudson?!?")
public class MetadataTest {

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
 void legacyContact() throws ServiceException {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  Map<String, Object> fields = new HashMap<String, Object>();
  String f1 = "First1";
  String l1 = "Last1";
  fields.put(ContactConstants.A_firstName, f1);
  fields.put(ContactConstants.A_lastName, l1);
  Contact contact = mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);

  assertEquals(f1, contact.get(ContactConstants.A_firstName));
  assertEquals(l1, contact.get(ContactConstants.A_lastName));

  Metadata metadata = new Metadata();
  String mailAddr = "test@email.net";
  String f2 = "First2";
  metadata.put(ContactConstants.A_email, mailAddr);
  metadata.put(ContactConstants.A_firstName, f2);

  contact.decodeMetadata(metadata);

  assertEquals(mailAddr, contact.get(ContactConstants.A_email));
  assertEquals(f2, contact.get(ContactConstants.A_firstName));
 }

 @Test
 void legacyCalendarItem() throws ServiceException, MessagingException {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  SetCalendarItemData defaultInv = new SetCalendarItemData();
  MimeMessage message = new JavaMailMimeMessage(JMSession.getSession(), new ZSharedFileInputStream("data/TestMailRaw/invite1"));
  defaultInv.message = new ParsedMessage(message, Calendar.getInstance().getTimeInMillis(), false);
  TimeZoneMap tzMap = new TimeZoneMap(WellKnownTimeZones.getTimeZoneById("EST"));
  Invite invite = new Invite("REQUEST", tzMap, false);
  invite.setUid("test-uid");
  Calendar cal = GregorianCalendar.getInstance();
  cal.set(2005, 1, 21);
  invite.setDtStart(ParsedDateTime.fromUTCTime(cal.getTimeInMillis()));
  cal.set(2005, 2, 21);
  invite.setDtEnd(ParsedDateTime.fromUTCTime(cal.getTimeInMillis()));
  defaultInv.invite = invite;
  CalendarItem calItem = mbox.setCalendarItem(null, Mailbox.ID_FOLDER_CALENDAR, 0, null, defaultInv, null, null, CalendarItem.NEXT_ALARM_KEEP_CURRENT);

  calItem.mData.dateChanged = (int) (cal.getTimeInMillis() / 1000L);
  Metadata meta = calItem.encodeMetadata();
  meta.remove(Metadata.FN_TZMAP);

  calItem.decodeMetadata(meta);

  assertEquals(0, calItem.getStartTime());
  assertEquals(0, calItem.getEndTime());

  meta.put(Metadata.FN_TZMAP, "foo"); //simulate existence of FN_TZMAP with bad content. In reality the metadata versions 4, 5, 6 had more subtle differences in invite encoding, but this provokes the exception we need

  calItem.decodeMetadata(meta);

  assertEquals(0, calItem.getStartTime());
  assertEquals(0, calItem.getEndTime());

  cal.set(2007, 2, 21);

  calItem.mData.dateChanged = (int) (cal.getTimeInMillis() / 1000L);

  boolean caught = false;
  try {
   calItem.decodeMetadata(meta);
  } catch (ServiceException se) {
   if (se.getCode().equalsIgnoreCase(ServiceException.INVALID_REQUEST)) {
    caught = true;
   }
  }
  assertTrue(caught, "new(er) appointment with bad metadata");
 }

 @Test
 void standardMetadataFormat() throws MailServiceException {
  String encoded = "d3:prt94:d1:X5:false1:fd1:a25:roland.schemers@gmail.com1:d6:Roland1:p15:Roland Schemerse2:noi2e1:vi10ee1:vi10ee";
  Metadata md = new Metadata(encoded);
  assertNotNull(md);
 }

 @Test
 void legacyBlobMetadataFormat() throws MailServiceException {
  String encoded = "s=43:Roland Schemers <roland.schemers@gmail.com>;rt=44:forward schemers <roland.schemers@gmail.com>;f=74:http://support.microsoft.com/default.aspx?scid=kb;en-us;177378&Product=iep;";
  Metadata md = new Metadata(encoded);
  assertNotNull(md);
 }
}
