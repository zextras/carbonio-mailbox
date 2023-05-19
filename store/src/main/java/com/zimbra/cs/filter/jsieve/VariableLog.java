// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import java.util.Iterator;

import org.apache.jsieve.Argument;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.Block;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.commands.extensions.Log;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.mail.MailAdapter;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.filter.FilterUtil;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.soap.mail.type.FilterAction;

public class VariableLog extends Log {
    private ZimbraMailAdapter mailAdapter = null;

    @Override 
    protected Object executeBasic(MailAdapter mail, Arguments arguments, Block block,
            SieveContext context) throws SieveException {

        if (!(mail instanceof ZimbraMailAdapter)) {
            return null;
        }

        this.mailAdapter = (ZimbraMailAdapter) mail;
        return super.executeBasic(mail, arguments, block, context);

    }

    @Override
    protected void log(String logLevel, String message, SieveContext context) throws SyntaxException {
        message = FilterUtil.replaceVariables(mailAdapter, message);
        super.log(logLevel, message, context);
    }

    @Override
    protected void validateArguments(Arguments arguments, SieveContext context) throws SieveException {
        if (arguments.getArgumentList().size() > 2) {
            throw new SyntaxException("Log: maximum 2 parameters allowed with Log");
        }
        boolean foundTagArg = false;
        int index = 0;
        Iterator<Argument> itr = arguments.getArgumentList().iterator();
        while (itr.hasNext()) {
            Argument arg = itr.next();
            index++;
            if (arg instanceof TagArgument) {
                if (foundTagArg) {
                    throw new SyntaxException("Log: Multiple log levels are not allowed.");
                }
                if (index > 1) {
                    throw new SyntaxException("Log: Log level must be mentioned before log message.");
                }
                TagArgument tag = (TagArgument) arg;
                if (!(tag.is(":" + FilterAction.LogAction.LogLevel.fatal)
                        || tag.is(":" + FilterAction.LogAction.LogLevel.error)
                        || tag.is(":" + FilterAction.LogAction.LogLevel.warn)
                        || tag.is(":" + FilterAction.LogAction.LogLevel.info)
                        || tag.is(":" + FilterAction.LogAction.LogLevel.debug)
                        || tag.is(":" + FilterAction.LogAction.LogLevel.trace)
                        )) {
                    throw new SyntaxException("Log: Invalid log level provided - " + tag.getTag());
                }
                foundTagArg = true;
            }
            if (index > 1 && !foundTagArg) {
                throw new SyntaxException("Log: Only 1 text message allowed with log statement.");
            }
        }
        ZimbraLog.filter.debug("Log: Validation successfful");
    }
}
