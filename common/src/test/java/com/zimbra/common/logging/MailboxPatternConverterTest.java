// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.util.ZimbraLog;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MailboxPatternConverterTest {

  private final MailboxPatternConverter converter = MailboxPatternConverter.newInstance(null);

  @AfterEach
  void clearContext() {
    ZimbraLog.clearContext();
  }

  /** What an appender sees: context captured when the event is created on the logging thread. */
  private static LogEvent captureEvent() {
    return Log4jLogEvent.newBuilder()
        .setContextData(
            ContextDataInjectorFactory.createInjector()
                .injectContextData(null, new SortedArrayStringMap()))
        .build();
  }

  private String format(LogEvent event) {
    StringBuilder sb = new StringBuilder();
    converter.format(event, sb);
    return sb.toString();
  }

  @Test
  void keepsContextOfTheLoggingThreadAfterItMovesOn() {
    ZimbraLog.addToContext(ZimbraLog.C_NAME, "user@example.com");
    LogEvent event = captureEvent();
    ZimbraLog.clearContext();

    assertEquals("name=user@example.com;", format(event));
  }

  @Test
  void printsNothingOnceContextIsCleared() {
    ZimbraLog.addToContext(ZimbraLog.C_NAME, "user@example.com");
    ZimbraLog.clearContext();

    assertEquals("", format(captureEvent()));
  }

  @Test
  void printsNothingOnceLastKeyIsRemoved() {
    ZimbraLog.addToContext(ZimbraLog.C_NAME, "user@example.com");
    ZimbraLog.removeFromContext(ZimbraLog.C_NAME);

    assertEquals("", format(captureEvent()));
  }

  @Test
  void printsNothingWithoutContextData() {
    assertEquals("", format(Log4jLogEvent.newBuilder().build()));
  }
}
