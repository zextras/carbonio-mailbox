// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import static com.zimbra.cs.filter.JsieveConfigMapHandler.CAPABILITY_EREJECT;

import com.zimbra.cs.filter.ZimbraMailAdapter;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.Block;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.commands.optional.Reject;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;

public class Ereject extends Reject {

  @Override
  protected Object executeBasic(
      MailAdapter mail, Arguments arguments, Block block, SieveContext context)
      throws SieveException {
    if (!(mail instanceof ZimbraMailAdapter)) {
      return null;
    }
    ZimbraMailAdapter mailAdapter = (ZimbraMailAdapter) mail;
    Require.checkCapability(mailAdapter, CAPABILITY_EREJECT);

    mailAdapter.setDiscardActionPresent();
    final String message =
        ((StringListArgument) arguments.getArgumentList().get(0)).getList().get(0);
    mail.addAction(new ActionEreject(message));
    return null;
  }
}
