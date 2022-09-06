// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import com.zimbra.common.mime.InternetAddress;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.cs.mailbox.ContactRankings;
import com.zimbra.cs.mailbox.Mailbox;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.jsieve.Argument;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

/**
 * SIEVE test that returns true if the email address in the specified header exists in the contact
 * ranking table.
 *
 * @author ysasaki
 */
public final class ContactRankingTest extends AbstractTest {
  private static final String IN = ":in";
  private List<String> headers;

  @Override
  protected void validateArguments(Arguments args, SieveContext ctx) throws SieveException {
    Iterator<Argument> itr = args.getArgumentList().iterator();
    if (itr.hasNext()) {
      Argument arg = itr.next();
      if (arg instanceof TagArgument) {
        TagArgument tag = (TagArgument) arg;
        if (tag.is(IN)) {
          if (itr.hasNext()) {
            arg = itr.next();
            if (arg instanceof StringListArgument) {
              headers = ((StringListArgument) arg).getList();
            } else {
              throw ctx.getCoordinate().syntaxException(IN + " is missing an argument");
            }
          } else {
            throw ctx.getCoordinate().syntaxException(IN + " is missing an argument");
          }
        } else {
          throw ctx.getCoordinate().syntaxException("Unknown tag: " + tag.getTag());
        }
      } else {
        throw ctx.getCoordinate().syntaxException("Unexpected argument: " + arg.getValue());
      }
    }
  }

  @Override
  protected boolean executeBasic(MailAdapter mail, Arguments args, SieveContext ctx)
      throws SieveException {
    assert (headers != null);
    if (mail instanceof DummyMailAdapter) {
      return true;
    }
    if (!(mail instanceof ZimbraMailAdapter)) {
      return false;
    }
    Mailbox mbox = ((ZimbraMailAdapter) mail).getMailbox();
    List<InternetAddress> addrs = new ArrayList<InternetAddress>();
    for (String header : headers) {
      for (String value : mail.getHeader(header)) {
        addrs.add(new InternetAddress(value));
      }
    }
    try {
      ContactRankings ranking = new ContactRankings(mbox.getAccountId());
      for (InternetAddress addr : addrs) {
        if (ranking.query(addr.getAddress()) > 0) {
          return true;
        }
      }
    } catch (ServiceException e) {
      ZimbraLog.filter.error("Failed to lookup ranking", e);
    }
    return false;
  }
}
