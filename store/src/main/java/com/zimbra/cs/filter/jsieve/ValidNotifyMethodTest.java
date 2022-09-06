// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import org.apache.jsieve.Argument;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

public class ValidNotifyMethodTest extends AbstractTest {

  @Override
  protected boolean executeBasic(MailAdapter mail, Arguments arguments, SieveContext context)
      throws SieveException {
    List<String> notificationUris = null;

    /*
     * Usage:  valid_notify_method <notification-uris: string-list>
     */
    ListIterator<Argument> argumentsIter = arguments.getArgumentList().listIterator();

    if (argumentsIter.hasNext()) {
      Argument argument = argumentsIter.next();
      if (argument instanceof StringListArgument) {
        notificationUris = ((StringListArgument) argument).getList();
      }
    }
    if (null == notificationUris) {
      throw context.getCoordinate().syntaxException("Expecting a StringList of notification-uris");
    }
    return test(notificationUris);
  }

  @Override
  protected void validateArguments(Arguments arguments, SieveContext context) {
    // override validation -- it's already done in executeBasic above
  }

  private boolean test(List<String> notificationUris) {
    for (final String uri : notificationUris) {
      try {
        URL url = new URL(uri);
      } catch (Exception e) {
        return false;
      }
    }
    return true;
  }
}
