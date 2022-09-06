// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import com.zimbra.cs.filter.FilterUtil;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import java.util.List;
import org.apache.jsieve.Argument;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.Block;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.commands.AbstractActionCommand;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.mail.MailAdapter;

public class Reply extends AbstractActionCommand {

  @Override
  protected Object executeBasic(
      MailAdapter mail, Arguments arguments, Block block, SieveContext context)
      throws SieveException {
    if (!(mail instanceof ZimbraMailAdapter)) {
      return null;
    }
    ZimbraMailAdapter mailAdapter = (ZimbraMailAdapter) mail;

    String bodyTemplate =
        FilterUtil.replaceVariables(
            mailAdapter,
            ((StringListArgument) arguments.getArgumentList().get(0)).getList().get(0));
    mail.addAction(new ActionReply(bodyTemplate));
    return null;
  }

  @Override
  protected void validateArguments(Arguments arguments, SieveContext context)
      throws SieveException {
    List<Argument> args = arguments.getArgumentList();
    if (args.size() != 1)
      throw new SyntaxException("Exactly 1 argument permitted. Found " + args.size());

    Argument argument = args.get(0);
    if (!(argument instanceof StringListArgument)) throw new SyntaxException("Expected text");

    if (((StringListArgument) argument).getList().size() != 1)
      throw new SyntaxException("Expected exactly one text");
  }
}
