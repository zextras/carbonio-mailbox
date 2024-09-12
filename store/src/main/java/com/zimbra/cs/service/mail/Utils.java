package com.zimbra.cs.service.mail;

import com.zimbra.common.calendar.ParsedDateTime;

class Utils {

  private Utils() {
    // Utility class
  }

  static boolean isValidDateTimeChange(ParsedDateTime startTime, ParsedDateTime newStartTime,
      ParsedDateTime endTime, ParsedDateTime newEndTime) {
    return startTime != null && newStartTime != null && endTime != null && newEndTime != null
        && (!startTime.equals(newStartTime) || !endTime.equals(newEndTime));
  }
}
