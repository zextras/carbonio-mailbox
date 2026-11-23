/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.calendar.ZCalendar.ZCalendarBuilder;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.calendar.Invite;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendarItemIndexTest extends MailboxTestSuite {

  private Account account;
  private Mailbox mbox;

  @BeforeEach
  void setUp() throws Exception {
    account = createAccount().create();
    mbox = MailboxManager.getInstance().getMailboxByAccount(account);
  }

  @Test
  void rescheduledAppointmentKeepsOneIndexRowWithNewTimes() throws Exception {
    int id = addEvent("U1", 0, "20300110T100000Z", "20300110T110000Z", "");

    addEvent("U1", 1, "20300210T100000Z", "20300210T110000Z", "");

    assertEquals(
        List.of("U1|" + id + "|2030-02-10T10:00:00Z|2030-02-10T11:00:00Z"), indexRows());
  }

  @Test
  void extendedRecurrenceMovesIndexEnd() throws Exception {
    int id = addEvent("U1", 0, "20300107T100000Z", "20300107T110000Z", "RRULE:FREQ=WEEKLY;COUNT=2\r\n");

    addEvent("U1", 1, "20300107T100000Z", "20300107T110000Z", "RRULE:FREQ=WEEKLY;COUNT=4\r\n");

    assertEquals(
        List.of("U1|" + id + "|2030-01-07T10:00:00Z|2030-01-28T11:00:00Z"), indexRows());
  }

  @Test
  void alarmBeforeStartMovesIndexStartToAlarm() throws Exception {
    int id = addEvent("U1", 0, "20300110T100000Z", "20300110T110000Z", alarm("-PT15M"));

    assertEquals(
        List.of("U1|" + id + "|2030-01-10T09:45:00Z|2030-01-10T11:00:00Z"), indexRows());
  }

  @Test
  void snoozedAlarmKeepsIndexStartAtEarliestAlarm() throws Exception {
    int id = addEvent("U1", 0, "20300110T100000Z", "20300110T110000Z", alarm("-PT15M"));

    mbox.snoozeCalendarItemAlarm(null, id, Instant.parse("2030-01-10T09:55:00Z").toEpochMilli());

    assertEquals(
        List.of("U1|" + id + "|2030-01-10T09:45:00Z|2030-01-10T11:00:00Z"), indexRows());
  }

  @Test
  void dismissedAlarmKeepsOneIndexRow() throws Exception {
    int id = addEvent("U1", 0, "20300110T100000Z", "20300110T110000Z", alarm("-PT15M"));

    mbox.dismissCalendarItemAlarm(null, id, Instant.parse("2030-01-10T09:45:00Z").toEpochMilli());

    assertEquals(1, indexRows().size());
  }

  private int addEvent(String uid, int sequence, String start, String end, String extra)
      throws Exception {
    String ical =
        "BEGIN:VCALENDAR\r\n"
            + "VERSION:2.0\r\n"
            + "PRODID:-//test//EN\r\n"
            + "METHOD:PUBLISH\r\n"
            + "BEGIN:VEVENT\r\n"
            + "UID:" + uid + "\r\n"
            + "SEQUENCE:" + sequence + "\r\n"
            + "DTSTAMP:20300101T000000Z\r\n"
            + "DTSTART:" + start + "\r\n"
            + "DTEND:" + end + "\r\n"
            + "SUMMARY:Test\r\n"
            + "ORGANIZER:mailto:" + account.getName() + "\r\n"
            + extra
            + "END:VEVENT\r\n"
            + "END:VCALENDAR\r\n";
    Invite invite =
        Invite.createFromCalendar(account, "", ZCalendarBuilder.build(ical), true).get(0);
    return mbox.addInvite(null, invite, Mailbox.ID_FOLDER_CALENDAR).calItemId;
  }

  private static String alarm(String trigger) {
    return "BEGIN:VALARM\r\n"
        + "ACTION:DISPLAY\r\n"
        + "DESCRIPTION:Reminder\r\n"
        + "TRIGGER:" + trigger + "\r\n"
        + "END:VALARM\r\n";
  }

  private List<String> indexRows() throws Exception {
    List<String> rows = new ArrayList<>();
    DbConnection conn = DbPool.getConnection(mbox);
    try (PreparedStatement stmt =
        conn.prepareStatement(
            "SELECT uid, item_id, start_time, end_time FROM "
                + DbMailItem.getCalendarItemTableName(mbox)
                + " WHERE mailbox_id = ? ORDER BY uid")) {
      stmt.setInt(1, mbox.getId());
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          rows.add(
              rs.getString(1)
                  + "|"
                  + rs.getInt(2)
                  + "|"
                  + rs.getTimestamp(3).toInstant()
                  + "|"
                  + rs.getTimestamp(4).toInstant());
        }
      }
    } finally {
      DbPool.quietClose(conn);
    }
    return rows;
  }
}
