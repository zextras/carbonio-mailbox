// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import java.util.List;

import org.apache.jsieve.Argument;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.Block;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.commands.AbstractActionCommand;
import org.apache.jsieve.mail.MailAdapter;

/**
 * @since Nov 8, 2004
 */
public final class Flag extends AbstractActionCommand {

    @Override
    protected Object executeBasic(MailAdapter mail, Arguments args, Block arg2, SieveContext context) {
        String flagName = ((StringListArgument) args.getArgumentList().get(0)).getList().get(0);
        ActionFlag action = ActionFlag.of(flagName);
        mail.addAction(action);
        return null;
    }

    @Override
    protected void validateArguments(Arguments arguments, SieveContext context) throws SieveException {
        List<Argument> args = arguments.getArgumentList();
        if (args.size() != 1) {
            throw new SyntaxException("Exactly 1 argument permitted. Found " + args.size());
        }
        Object argument = args.get(0);
        if (!(argument instanceof StringListArgument)) {
            throw new SyntaxException("Expecting a string-list");
        }
        List<String> strList = ((StringListArgument) argument).getList();
        if (1 != strList.size()) {
            throw new SyntaxException("Expecting exactly one argument");
        }
        String flagName = strList.get(0);
        if (ActionFlag.of(flagName.toLowerCase()) == null) {
            throw new SyntaxException("Invalid flag: " + flagName);
        }
    }
}
