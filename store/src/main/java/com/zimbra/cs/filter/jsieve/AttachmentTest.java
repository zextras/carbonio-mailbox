// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Nov 11, 2004
 *
 */
package com.zimbra.cs.filter.jsieve;

import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.cs.mime.ParsedMessage;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

public class AttachmentTest extends AbstractTest {

  @Override
  protected boolean executeBasic(MailAdapter mail, Arguments arguments, SieveContext context) {
    if (mail instanceof DummyMailAdapter) {
      return true;
    }
    if (!(mail instanceof ZimbraMailAdapter)) {
      return false;
    }
    ParsedMessage pm = ((ZimbraMailAdapter) mail).getParsedMessage();
    if (pm == null) {
      return false;
    } else {
      return pm.hasAttachments();
    }
  }
}
