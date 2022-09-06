// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.cs.mime.ParsedAddress;
import java.util.Set;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

/**
 * SIEVE test for LinkedIn notifications.
 *
 * <p>Built-in test for LinkedIn notifications:
 *
 * <ul>
 *   <li>from {@code member@linkedin.com}
 *   <li>from {@code connections@linkedin.com}
 * </ul>
 *
 * @author ysasaki
 */
public final class LinkedInTest extends AbstractTest {
  private static final Set<String> ADDRESSES =
      ImmutableSet.of("connections@linkedin.com", "member@linkedin.com", "hit-reply@linkedin.com");

  @Override
  protected boolean executeBasic(MailAdapter mail, Arguments args, SieveContext ctx)
      throws SieveException {
    if (mail instanceof DummyMailAdapter) {
      return true;
    }
    if (!(mail instanceof ZimbraMailAdapter)) {
      return false;
    }
    ZimbraMailAdapter adapter = (ZimbraMailAdapter) mail;

    ParsedAddress sender = adapter.getParsedMessage().getParsedSender();
    if (!Strings.isNullOrEmpty(sender.emailPart)
        && ADDRESSES.contains(sender.emailPart.toLowerCase())) {
      return true;
    }
    return false;
  }
}
