package com.zimbra.cs.dav.caldav;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zimbra.common.calendar.TimeZoneMap;
import com.zimbra.common.calendar.ZCalendar.ICalTok;
import com.zimbra.common.calendar.ZCalendar.ScheduleAgent;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.caldav.AutoScheduler.OrganizerAutoScheduler;
import com.zimbra.cs.mailbox.CalendarItem.ReplyInfo;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.SetCalendarItemData;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.ZAttendee;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.mail.Address;
import org.junit.Before;
import org.junit.Test;

public class OrganizerAutoSchedulerTest {

  private Provisioning prov;
  private Account organizer;
  private Account attendee1;
  private Account attendee2;
  private Account attendee3;

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    prov = Provisioning.getInstance();
    organizer =
        prov.createAccount(
            "organizer@test.io",
            "secret",
            new HashMap<>() {
              {
                put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
              }
            });
    attendee1 =
        prov.createAccount(
            "attendee1@test.io",
            "secret",
            new HashMap<>() {
              {
                put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
              }
            });
    attendee2 =
        prov.createAccount(
            "attendee2@test.io",
            "secret",
            new HashMap<>() {
              {
                put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
              }
            });
    attendee3 =
        prov.createAccount(
            "attendee3@test.io",
            "secret",
            new HashMap<>() {
              {
                put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
              }
            });
  }

  /**
   * Test for CO-491 meeting with SCHEDULE-AGENT=CLIENT. attendee3@test.io has SCHEDULE-AGENT=CLIENT
   * so no message autoschedule from server for it. Per RFC this is not a realistic scenario as all
   * attendees and organizer should have the same SCHEDULE-AGENT.
   */
  @Test
  public void shouldReturnAddressesWithoutScheduleAgentClientFromAttendees() throws Exception {
    final Invite invite = new Invite(ICalTok.REQUEST.toString(), mock(TimeZoneMap.class), true);
    final ZAttendee zAttendee1 = mock(ZAttendee.class);
    when(zAttendee1.getAddress()).thenReturn(attendee1.getName());
    final ZAttendee zAttendee2 = mock(ZAttendee.class);
    when(zAttendee2.getAddress()).thenReturn(attendee2.getName());
    final ZAttendee zAttendee3 = mock(ZAttendee.class);
    when(zAttendee3.getAddress()).thenReturn(attendee3.getName());
    // Attendee3 has SCHEDULE-AGENT=CLIENT
    when(zAttendee3.getScheduleAgent()).thenReturn("CLIENT");
    invite.addAttendee(zAttendee1);
    invite.addAttendee(zAttendee2);
    invite.addAttendee(zAttendee3);
    final ArrayList<ZAttendee> attendees =
        new ArrayList<>() {
          {
            add(zAttendee1);
            add(zAttendee2);
            add(zAttendee3);
          }
        };
    final DavContext mockDavContext = mock(DavContext.class);
    SetCalendarItemData setCalendarItemData = new SetCalendarItemData();
    setCalendarItemData.invite = invite;
    when(mockDavContext.getAuthAccount()).thenReturn(organizer);
    final Mailbox mailboxByAccount = MailboxManager.getInstance().getMailboxByAccount(organizer);
    final List<Address> autoScheduledAttendeesAddress =
        new OrganizerAutoScheduler(
                mailboxByAccount,
                mailboxByAccount,
                new Invite[] {invite},
                1,
                1,
                new String[] {},
                setCalendarItemData,
                new SetCalendarItemData[] {setCalendarItemData},
                new ArrayList<ReplyInfo>() {
                  {
                    add(mock(ReplyInfo.class));
                  }
                },
                mockDavContext)
            .getRecipientsForAttendees(attendees);
    final Address[] expected =
        new Address[] {zAttendee1.getFriendlyAddress(), zAttendee2.getFriendlyAddress()};
    assertArrayEquals(expected, autoScheduledAttendeesAddress.toArray());
  }

  /** Test for CO-491 meeting when SCHEDULE-AGENT not set in invite */
  @Test
  public void shouldReturnAllAttendeesWhenScheduleAgentClientNotSet() throws Exception {
    final Invite invite = new Invite(ICalTok.REQUEST.toString(), mock(TimeZoneMap.class), true);
    final ZAttendee zAttendee1 = mock(ZAttendee.class);
    when(zAttendee1.getAddress()).thenReturn(attendee1.getName());
    final ZAttendee zAttendee2 = mock(ZAttendee.class);
    when(zAttendee2.getAddress()).thenReturn(attendee2.getName());
    final ZAttendee zAttendee3 = mock(ZAttendee.class);
    when(zAttendee3.getAddress()).thenReturn(attendee3.getName());
    invite.addAttendee(zAttendee1);
    invite.addAttendee(zAttendee2);
    invite.addAttendee(zAttendee3);
    final ArrayList<ZAttendee> attendees =
        new ArrayList<>() {
          {
            add(zAttendee1);
            add(zAttendee2);
            add(zAttendee3);
          }
        };
    final DavContext mockDavContext = mock(DavContext.class);
    SetCalendarItemData setCalendarItemData = new SetCalendarItemData();
    setCalendarItemData.invite = invite;
    when(mockDavContext.getAuthAccount()).thenReturn(organizer);
    final Mailbox mailboxByAccount = MailboxManager.getInstance().getMailboxByAccount(organizer);
    final List<Address> autoScheduledAttendeesAddress =
        new OrganizerAutoScheduler(
                mailboxByAccount,
                mailboxByAccount,
                new Invite[] {invite},
                1,
                1,
                new String[] {},
                setCalendarItemData,
                new SetCalendarItemData[] {setCalendarItemData},
                new ArrayList<ReplyInfo>() {
                  {
                    add(mock(ReplyInfo.class));
                  }
                },
                mockDavContext)
            .getRecipientsForAttendees(attendees);
    final Address[] expected =
        new Address[] {
          zAttendee1.getFriendlyAddress(),
          zAttendee2.getFriendlyAddress(),
          zAttendee3.getFriendlyAddress()
        };
    assertArrayEquals(expected, autoScheduledAttendeesAddress.toArray());
  }

  /** When invite attendee has SCHEDULE-AGENT=NONE do not schedule message */
  @Test
  public void shouldReturnNoAttendeesFromInviteWhenAllHaveScheduleAgentNone() throws Exception {
    final Invite invite = new Invite(ICalTok.REQUEST.toString(), mock(TimeZoneMap.class), true);
    final ZAttendee zAttendee1 = mock(ZAttendee.class);
    when(zAttendee1.getAddress()).thenReturn(attendee1.getName());
    when(zAttendee1.getScheduleAgent()).thenReturn(ScheduleAgent.NONE.toString());
    final ZAttendee zAttendee2 = mock(ZAttendee.class);
    when(zAttendee2.getAddress()).thenReturn(attendee2.getName());
    when(zAttendee2.getScheduleAgent()).thenReturn(ScheduleAgent.NONE.toString());
    final ZAttendee zAttendee3 = mock(ZAttendee.class);
    when(zAttendee3.getAddress()).thenReturn(attendee3.getName());
    when(zAttendee3.getScheduleAgent()).thenReturn(ScheduleAgent.NONE.toString());
    invite.addAttendee(zAttendee1);
    invite.addAttendee(zAttendee2);
    invite.addAttendee(zAttendee3);
    final DavContext mockDavContext = mock(DavContext.class);
    SetCalendarItemData setCalendarItemData = new SetCalendarItemData();
    setCalendarItemData.invite = invite;
    when(mockDavContext.getAuthAccount()).thenReturn(organizer);
    final Mailbox mailboxByAccount = MailboxManager.getInstance().getMailboxByAccount(organizer);
    final List<Address> autoScheduledAttendeesAddress =
        new OrganizerAutoScheduler(
                mailboxByAccount,
                mailboxByAccount,
                new Invite[] {invite},
                1,
                1,
                new String[] {},
                setCalendarItemData,
                new SetCalendarItemData[] {setCalendarItemData},
                new ArrayList<ReplyInfo>() {
                  {
                    add(mock(ReplyInfo.class));
                  }
                },
                mockDavContext)
            .getRecipientsForAttendees(invite);
    assertEquals(0, autoScheduledAttendeesAddress.size());
  }
}
