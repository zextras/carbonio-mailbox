package com.zimbra.common.calendar;

import static org.junit.Assert.*;

import com.zimbra.common.calendar.ZCalendar.ScheduleAgent;
import org.junit.Test;

public class ZCalendarTest {

  public static class ScheduleAgentTest {

    @Test
    public void shouldReturnServerWhenCaseInsensitiveServer() {
      assertEquals(ScheduleAgent.SERVER, ScheduleAgent.getScheduleAgent("serVER"));
    }

    @Test
    public void shouldReturnClientWhenCaseInsensitiveClient() {
      assertEquals(ScheduleAgent.CLIENT, ScheduleAgent.getScheduleAgent("cLIENT"));
    }

    @Test
    public void shouldReturnNoneWhenCaseInsensitiveNone() {
      assertEquals(ScheduleAgent.NONE, ScheduleAgent.getScheduleAgent("None"));
    }

    @Test
    public void shouldReturnNullWhenInputNull() {
      assertNull(ScheduleAgent.getScheduleAgent(null));
    }

    @Test
    public void shouldReturnNullWhenNoMatch() {
      assertNull(ScheduleAgent.getScheduleAgent("wonderIfThisIsAccepted"));
    }
  }
}
