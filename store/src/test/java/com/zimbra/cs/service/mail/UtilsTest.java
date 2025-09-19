package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.calendar.ParsedDateTime;
import java.text.ParseException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class UtilsTest {

  @ParameterizedTest
  @MethodSource("provideTimeChangeScenarios")
  void testValidTimeChange(ParsedDateTime startTime, ParsedDateTime newStartTime,
      ParsedDateTime endTime, ParsedDateTime newEndTime,
      boolean expectedResult) {

    boolean result = Utils.isValidDateTimeChange(startTime, newStartTime, endTime, newEndTime);

    assertEquals(expectedResult, result);
  }

  private static Stream<Object[]> provideTimeChangeScenarios() throws ParseException {
    return Stream.of(
        // Different start times should return true
        new Object[]{ParsedDateTime.parseUtcOnly("20250907T163000"), ParsedDateTime.parseUtcOnly("20250907T164000"),
            ParsedDateTime.parseUtcOnly("20250907T173000"), ParsedDateTime.parseUtcOnly("20250907T173000"), true},

        // Different end times should return true
        new Object[]{ParsedDateTime.parseUtcOnly("20250907T154000"), ParsedDateTime.parseUtcOnly("20250907T154000"),
            ParsedDateTime.parseUtcOnly("20250907T163000"), ParsedDateTime.parseUtcOnly("20250907T164000"), true},

        // Same start and end times should return false
        new Object[]{ParsedDateTime.parseUtcOnly("20250907T163000"), ParsedDateTime.parseUtcOnly("20250907T163000"),
            ParsedDateTime.parseUtcOnly("20250907T164000"), ParsedDateTime.parseUtcOnly("20250907T164000"), false},

        // Null start time should return false
        new Object[]{null,  ParsedDateTime.parseUtcOnly("20250907T163000"),
            ParsedDateTime.parseUtcOnly("20250907T164000"), ParsedDateTime.parseUtcOnly("20250907T164000"), false},

        // Null end time should return false
        new Object[]{ParsedDateTime.parseUtcOnly("20250907T164000"), ParsedDateTime.parseUtcOnly("20250907T164000"),
            ParsedDateTime.parseUtcOnly("20250907T165000"), null, false},

        // All times null should return false
        new Object[]{null, null, null, null, false}
    );
  }


}