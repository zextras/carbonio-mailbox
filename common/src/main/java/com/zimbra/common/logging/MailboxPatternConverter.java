// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.logging;

import com.zimbra.common.util.ZimbraLog;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

/**
 * Pattern Layout to expand %z in log4j2.
 *
 * @see org.apache.logging.log4j.core.layout.PatternLayout Also:
 *     https://logging.apache.org/log4j/2.x/manual/extending.html#patternconverters
 * @author davidefrison
 * @since 23.8.0
 */
@Plugin(name = "MailboxPatternConverter", category = "Converter")
@ConverterKeys({"z"})
public class MailboxPatternConverter extends LogEventPatternConverter {

  /** Constructs an instance of LoggingEventPatternConverter. */
  public MailboxPatternConverter(String[] options) {
    super("MailboxConverter", null);
  }

  /**
   * Obtains an instance of pattern converter.
   *
   * @param options options, may be null.
   * @return instance of pattern converter.
   */
  public static MailboxPatternConverter newInstance(final String[] options) {
    return new MailboxPatternConverter(options);
  }

  @Override
  public void format(LogEvent event, StringBuilder toAppendTo) {
    toAppendTo.append(ZimbraLog.getContextString() == null ? "" : ZimbraLog.getContextString());
  }
}
