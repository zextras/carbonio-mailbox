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
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.commands.AbstractActionCommand;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.mail.MailAdapter;

public class Notify extends AbstractActionCommand {

  @Override
  protected Object executeBasic(
      MailAdapter mail, Arguments arguments, Block block, SieveContext context)
      throws SieveException {
    if (!(mail instanceof ZimbraMailAdapter)) {
      return null;
    }
    ZimbraMailAdapter mailAdapter = (ZimbraMailAdapter) mail;

    List<Argument> args = arguments.getArgumentList();
    if (args.size() < 3) throw new SyntaxException("Missing arguments");

    Argument nextArg = args.get(0);
    if (!(nextArg instanceof StringListArgument)) throw new SyntaxException("Expected string");
    List<String> list = ((StringListArgument) nextArg).getList();
    if (list.size() != 1) throw new SyntaxException("Expected exactly one email address");
    String emailAddr = FilterUtil.replaceVariables(mailAdapter, list.get(0));

    nextArg = args.get(1);
    if (!(nextArg instanceof StringListArgument)) throw new SyntaxException("Expected string");
    list = ((StringListArgument) nextArg).getList();
    if (list.size() != 1) throw new SyntaxException("Expected exactly one subject");
    String subjectTemplate = FilterUtil.replaceVariables(mailAdapter, list.get(0));

    nextArg = args.get(2);
    if (!(nextArg instanceof StringListArgument)) throw new SyntaxException("Expected string");
    list = ((StringListArgument) nextArg).getList();
    if (list.size() != 1) throw new SyntaxException("Expected exactly one body");
    String bodyTemplate = FilterUtil.replaceVariables(mailAdapter, list.get(0));

    int maxBodyBytes = -1;
    List<String> origHeaders = null;
    if (args.size() == 4) {
      nextArg = args.get(3);
      if (nextArg instanceof NumberArgument) maxBodyBytes = ((NumberArgument) nextArg).getInteger();
      else if (nextArg instanceof StringListArgument)
        origHeaders = ((StringListArgument) nextArg).getList();
      else throw new SyntaxException("Invalid argument");
    }

    if (args.size() == 5) {
      nextArg = args.get(3);
      if (!(nextArg instanceof NumberArgument)) throw new SyntaxException("Expected int");
      maxBodyBytes = ((NumberArgument) nextArg).getInteger();
      nextArg = args.get(4);
      if (!(nextArg instanceof StringListArgument))
        throw new SyntaxException("Expected string list");
      origHeaders = ((StringListArgument) nextArg).getList();
    }

    mail.addAction(
        new ActionNotify(emailAddr, subjectTemplate, bodyTemplate, maxBodyBytes, origHeaders));
    return null;
  }

  @Override
  protected void validateArguments(Arguments arguments, SieveContext context)
      throws SieveException {
    // done in executeBasic()
  }
}
