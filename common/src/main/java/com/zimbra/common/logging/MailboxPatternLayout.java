// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.common.logging;

import com.zimbra.common.util.ZimbraLog;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

/**
 * Pattern Layout to expand %z in log4j2.
 * @see org.apache.logging.log4j.core.layout.PatternLayout
 *
 * @author davidefrison
 * @since 23.8.0
 */
@Plugin(name = "MailboxPatternLayout", category = "Converter")
@ConverterKeys({ "z" })
public class MailboxPatternLayout extends LogEventPatternConverter {


  /**
   * Constructs an instance of LoggingEventPatternConverter.
   *
   * @param name  name of converter.
   * @param style CSS style for output.
   */
  protected MailboxPatternLayout(String name, String style) {
    super(name, style);
  }

  @Override
  public void format(LogEvent event, StringBuilder toAppendTo) {
    toAppendTo.append(ZimbraLog.getContextString() == null ? "" : ZimbraLog.getContextString());
  }
}