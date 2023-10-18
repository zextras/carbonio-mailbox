// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.fb;

import com.zimbra.cs.fb.FreeBusy.Method;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FreeBusyTest {

  @Test
  void shouldSetUidWhenProvided() {
    final String uid = UUID.randomUUID().toString();
    final String attendee = "attendee@test.com";
    final String vCalendarString =
        new FreeBusy(attendee, System.currentTimeMillis(), System.currentTimeMillis())
            .toVCalendar(Method.REPLY, "organizer@test.com", attendee, null, uid);
    Assertions.assertTrue(vCalendarString.contains("UID:" + uid));
  }

  private static Stream<Arguments> ignoreMailToTest() {
    return Stream.of(
        Arguments.of(
            "organizer@test.com",
            "attendee@test.com",
            "mailto:organizer@test.com",
            "mailto:attendee@test.com"),
        Arguments.of(
            "mailto:organizer@test.com",
            "mailto:attendee@test.com",
            "mailto:organizer@test.com",
            "mailto:attendee@test.com"),
        Arguments.of(
            "mailto:organizer@test.com",
            "attendee@test.com",
            "mailto:organizer@test.com",
            "mailto:attendee@test.com"),
        Arguments.of(
            "organizer@test.com",
            "mailto:attendee@test.com",
            "mailto:organizer@test.com",
            "mailto:attendee@test.com"));
  }

  @ParameterizedTest
  @MethodSource("ignoreMailToTest")
  @DisplayName(
      "Ff organizer or attendee are provided with mailto: it does not affect the returned value")
  void shouldIgnoreMailToInAttendeeAndOrganizer(
      String attendee, String organizer, String expectedAttendee, String expectedOrganizer) {
    final String vCalendarString =
        new FreeBusy(attendee, System.currentTimeMillis(), System.currentTimeMillis())
            .toVCalendar(Method.REPLY, organizer, attendee, null, null);
    Assertions.assertTrue(vCalendarString.contains("ATTENDEE:" + expectedAttendee));
    Assertions.assertTrue(vCalendarString.contains("ORGANIZER:" + expectedOrganizer));
  }
}
