// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

/**
 * SIEVE test whether or not the message is to a mailing list or distribution list the user belongs
 * to.
 *
 * <p>The presence of List-Id header (RFC 2919) is a clear indicator, however some mailing list
 * distribution software including Zimbra haven't adopted it. {@link ListTest} returns true if any
 * of the following conditions are met:
 *
 * <ul>
 *   <li>{@code X-Zimbra-DL} header exists
 *   <li>{@code List-Id} header exists
 * </ul>
 *
 * @see http://www.ietf.org/rfc/rfc3685.txt
 * @author ysasaki
 */
public final class ListTest extends AbstractTest {

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
    if (!adapter.getHeader("X-Zimbra-DL").isEmpty() || !adapter.getHeader("List-Id").isEmpty()) {
      return true;
    }
    return false;
  }
}
