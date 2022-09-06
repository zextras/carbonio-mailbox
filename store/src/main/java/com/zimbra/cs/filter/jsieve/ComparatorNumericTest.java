// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import static com.zimbra.cs.filter.JsieveConfigMapHandler.CAPABILITY_COMPARATOR_NUMERIC;

import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

public class ComparatorNumericTest extends AbstractTest {
  @Override
  protected boolean executeBasic(MailAdapter mail, Arguments arguments, SieveContext context)
      throws SieveException {
    throw new SyntaxException("Unexpected test " + CAPABILITY_COMPARATOR_NUMERIC);
  }
}
