// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator.Factory;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.service.mail.message.parser.InviteParserResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CalendarUtilsTest {

  private static Factory accountCreatorFactory;

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    accountCreatorFactory = Factory.getDefault();
  }

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void parseInviteRaw_shouldParseInvite() throws Exception {
    final Account account = accountCreatorFactory.get().create();
    final XMLElement inviteElement = new XMLElement(MailConstants.E_INVITE);
    final Element content = inviteElement.addUniqueElement(MailConstants.E_CONTENT);
    final String uid = "21B97459-D97B-4B23-AF2A-E2759745C299";
    final String summary = "Meeting";
    final String endDate = "20170515T110000";
    final String startDate = "20170515T100000";
    content.setText("BEGIN:VCALENDAR\n"
            + "BEGIN:VEVENT\n"
            + "CREATED:20151219T021727Z\n"
            + "DTEND;TZID=America/Toronto:" + endDate + "\n"
            + "DTSTAMP:20151219T021727Z\n"
            + "DTSTART;TZID=America/Toronto:" + startDate + "\n"
            + "LAST-MODIFIED:20151219T021727Z\n"
            + "RRULE:FREQ=DAILY;UNTIL=20170519T035959Z\n"
            + "SEQUENCE:0\n"
            + "SUMMARY:" + summary + "\n"
            + "TRANSP:OPAQUE\n"
            + "UID:" + uid + "\n"
            + "END:VEVENT\n"
            + "END:VCALENDAR");
    content.addAttribute(MailConstants.A_SUMMARY, "Test appointment");
    content.addAttribute(MailConstants.A_UID, "calendar-uid");

    final InviteParserResult inviteParserResult = CalendarUtils.parseInviteRaw(account, inviteElement);

    Assertions.assertEquals(uid, inviteParserResult.mInvite.getUid());
    Assertions.assertEquals(0, inviteParserResult.mInvite.getSeqNo());
    Assertions.assertEquals(startDate + "Z", inviteParserResult.mInvite.getStartTime().toString());
    Assertions.assertEquals(endDate + "Z", inviteParserResult.mInvite.getEndTime().toString());
    Assertions.assertEquals(summary, inviteParserResult.mInvite.getName());
    Assertions.assertTrue(inviteParserResult.mInvite.isRecurrence());
  }

}