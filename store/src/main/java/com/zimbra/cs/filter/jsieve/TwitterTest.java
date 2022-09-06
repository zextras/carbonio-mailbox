// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import com.google.common.base.Strings;
import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.cs.mime.ParsedAddress;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

/**
 * SIEVE test for Twitter notifications.
 *
 * <p>Built-in test for Twitter notifications:
 *
 * <ul>
 *   <li>direct message email - {@code From: <dm-*@postmaster.twitter.com>}
 *   <li>mention email - {@code From: <mention-*@postmaster.twitter.com>}
 * </ul>
 *
 * @author ysasaki
 */
public final class TwitterTest extends AbstractTest {

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
    if (!Strings.isNullOrEmpty(sender.emailPart)) {
      String email = sender.emailPart.toLowerCase();
      if (email.equals("notify@twitter.com")) {
        return true;
      }
    }
    return false;
  }
}
