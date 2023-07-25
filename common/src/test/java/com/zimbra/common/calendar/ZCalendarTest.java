package com.zimbra.common.calendar;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.calendar.ZCalendar.ScheduleAgent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ZCalendarTest {

  @Nested
  public class ScheduleAgentTest {

    @Test
    void shouldReturnServerWhenCaseInsensitiveServer() {
      assertEquals(ScheduleAgent.SERVER, ScheduleAgent.getScheduleAgent("serVER"));
    }

    @Test
    void shouldReturnClientWhenCaseInsensitiveClient() {
      assertEquals(ScheduleAgent.CLIENT, ScheduleAgent.getScheduleAgent("cLIENT"));
    }

    @Test
    void shouldReturnNoneWhenCaseInsensitiveNone() {
      assertEquals(ScheduleAgent.NONE, ScheduleAgent.getScheduleAgent("None"));
    }

    @Test
    void shouldReturnNullWhenInputNull() {
      assertNull(ScheduleAgent.getScheduleAgent(null));
    }

    @Test
    void shouldReturnNullWhenNoMatch() {
      assertNull(ScheduleAgent.getScheduleAgent("wonderIfThisIsAccepted"));
    }
  }
}
