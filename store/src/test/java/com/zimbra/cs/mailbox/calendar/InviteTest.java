package com.zimbra.cs.mailbox.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.calendar.ZCalendar.ScheduleAgent;
import com.zimbra.common.calendar.ZCalendar.ZCalendarBuilder;
import com.zimbra.common.calendar.ZCalendar.ZVCalendar;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class InviteTest {

  public InviteTest(String scheduleAgentParam, ScheduleAgent scheduleAgentExpected) {
    this.scheduleAgentParam = scheduleAgentParam;
    this.scheduleAgentExpected = scheduleAgentExpected;
  }

  private Provisioning prov;
  private ScheduleAgent scheduleAgentExpected;
  private String scheduleAgentParam;

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    prov = Provisioning.getInstance();
  }

  @Parameters
  public static Collection<Object[]> scheduleAgentParamData() {
    // The ";" is added to avoid error in building the iCalendar request
    return Arrays.asList(
        new Object[][] {
          {";SCHEDULE-AGENT=SERVER", ScheduleAgent.SERVER},
          {";SCHEDULE-AGENT=CLIENT", ScheduleAgent.CLIENT},
          {";SCHEDULE-AGENT=serVeR", ScheduleAgent.SERVER},
          {";SCHEDULE-AGENT=client", ScheduleAgent.CLIENT},
          {"", ScheduleAgent.SERVER},
          {";SCHEDULE-AGENT=NONE", ScheduleAgent.NONE},
          {";SCHEDULE-AGENT=None", ScheduleAgent.NONE},
        });
  }

 /**
  * Test ScheduleAgent is set correctly in invite both on organizer and attendee
  *
  * @throws Exception
   */
 /*~~(Recipe failed with an exception.
java.lang.NullPointerException: null
  java.base/java.util.Objects.requireNonNull(Objects.java:221)
  org.openrewrite.Parser$Input.fromResource(Parser.java:176)
  org.openrewrite.Parser$Input.fromResource(Parser.java:171)
  org.openrewrite.java.testing.junit5.ParameterizedRunnerToParameterized$ParameterizedRunnerToParameterizedTestsVisitor.lambda$static$0(ParameterizedRunnerToParameterized.java:154)
  org.openrewrite.java.internal.template.JavaTemplateParser.compileTemplate(JavaTemplateParser.java:247)
  org.openrewrite.java.internal.template.JavaTemplateParser.lambda$parseAnnotations$7(JavaTemplateParser.java:207)
  org.openrewrite.java.internal.template.JavaTemplateParser.cache(JavaTemplateParser.java:256)
  org.openrewrite.java.internal.template.JavaTemplateParser.parseAnnotations(JavaTemplateParser.java:204)
  ...)~~>*/@Test
 void shouldReturnInviteWithCorrectScheduleAgent() throws Exception {

  final String organizerUuid = UUID.randomUUID().toString();
  Account organizer =
    prov.createAccount(
      organizerUuid + "@test.io",
      "secret",
      new HashMap<>() {
       {
        put(Provisioning.A_zimbraId, organizerUuid);
       }
      });
  final String attendeeUuid = UUID.randomUUID().toString();
  Account attendee =
    prov.createAccount(
      attendeeUuid + "@test.io",
      "secret",
      new HashMap<>() {
       {
        put(Provisioning.A_zimbraId, attendeeUuid);
       }
      });
  final String meetingCalendarRequest =
    "BEGIN:VCALENDAR\r\n"
      + "PRODID:-//Microsoft Corporation//Outlook 16.0 MIMEDIR//EN\r\n"
      + "VERSION:2.0\r\n"
      + "METHOD:REQUEST\r\n"
      + "X-MS-OLK-FORCEINSPECTOROPEN:TRUE\r\n"
      + "BEGIN:VTIMEZONE\r\n"
      + "TZID:Morocco Standard Time\r\n"
      + "BEGIN:STANDARD\r\n"
      + "DTSTART:16010318T030000\r\n"
      + "RRULE:FREQ=YEARLY;BYDAY=3SU;BYMONTH=3\r\n"
      + "TZOFFSETFROM:+0100\r\n"
      + "TZOFFSETTO:-0000\r\n"
      + "END:STANDARD\r\n"
      + "BEGIN:DAYLIGHT\r\n"
      + "DTSTART:16010422T020000\r\n"
      + "RRULE:FREQ=YEARLY;BYDAY=4SU;BYMONTH=4\r\n"
      + "TZOFFSETFROM:-0000\r\n"
      + "TZOFFSETTO:+0100\r\n"
      + "END:DAYLIGHT\r\n"
      + "END:VTIMEZONE\r\n"
      + "BEGIN:VTIMEZONE\r\n"
      + "TZID:Unnamed Time Zone 1\r\n"
      + "BEGIN:STANDARD\r\n"
      + "DTSTART:16011028T030000\r\n"
      + "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n"
      + "TZOFFSETFROM:+0100\r\n"
      + "TZOFFSETTO:-0000\r\n"
      + "END:STANDARD\r\n"
      + "BEGIN:DAYLIGHT\r\n"
      + "DTSTART:16010325T020000\r\n"
      + "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n"
      + "TZOFFSETFROM:-0000\r\n"
      + "TZOFFSETTO:+0100\r\n"
      + "END:DAYLIGHT\r\n"
      + "END:VTIMEZONE\r\n"
      + "BEGIN:VEVENT\r\n"
      + "ATTENDEE;CN="
      + attendee.getName()
      + ";RSVP=TRUE"
      + scheduleAgentParam
      + ":mailto:"
      + attendee.getName()
      + "\r\n"
      + "CLASS:PUBLIC\r\n"
      + "CREATED:20230208T182547Z\r\n"
      + "DESCRIPTION:Test\\r\n\\r\n\r\n"
      + "DTEND;TZID=\"Unnamed Time Zone 1\":20230208T073000\r\n"
      + "DTSTAMP:20230208T172547Z\r\n"
      + "DTSTART;TZID=\"Morocco Standard Time\":20230208T080000\r\n"
      + "LAST-MODIFIED:20230208T182547Z\r\n"
      + "ORGANIZER;CN=Test"
      + scheduleAgentParam
      + ":mailto:"
      + organizer.getName()
      + "\r\n"
      + "PRIORITY:5\r\n"
      + "SEQUENCE:0\r\n"
      + "SUMMARY;LANGUAGE=en-us:Test Calendar\r\n"
      + "TRANSP:OPAQUE\r\n"
      + "UID:040000008200E00074C5B7101A82E0080000000030F0C4C3EA3BD901000000000000000\r\n"
      + "\t010000000AD1EC2D87C850E46B5E017F88743C690\r\n"
      + "BEGIN:VALARM\r\n"
      + "TRIGGER:-PT15M\r\n"
      + "ACTION:DISPLAY\r\n"
      + "DESCRIPTION:Reminder\r\n"
      + "END:VALARM\r\n"
      + "END:VEVENT\r\n"
      + "END:VCALENDAR";
  final ZVCalendar zvCalendar = ZCalendarBuilder.build(meetingCalendarRequest);
  final List<Invite> inviteList = Invite.createFromCalendar(organizer, "", zvCalendar, true);
  assertEquals(1, inviteList.size());
  assertEquals(scheduleAgentExpected, inviteList.get(0).getOrganizer().getScheduleAgent());
  assertEquals(1, inviteList.get(0).getAttendees().size());
  assertEquals(scheduleAgentExpected, inviteList.get(0).getAttendees().get(0).getScheduleAgent());
 }
}
