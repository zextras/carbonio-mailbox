// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Nov 11, 2004
 *
 */
package com.zimbra.cs.filter.jsieve;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.cs.filter.ZimbraSieveException;
import com.zimbra.cs.mailbox.calendar.Util;

import org.apache.jsieve.Argument;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TimeZone;

public class CurrentDayOfWeekTest extends AbstractTest {

    @Override
    protected boolean executeBasic(MailAdapter mail, Arguments arguments, SieveContext context)
            throws SieveException {
        if (mail instanceof DummyMailAdapter) {
            return true;
        }
        if (!(mail instanceof ZimbraMailAdapter)) {
            return false;
        }

        ListIterator<Argument> argumentsIter = arguments.getArgumentList().listIterator();

        // First argument MUST be a ":is" tag
        String comparator = null;
        if (argumentsIter.hasNext()) {
            Object argument = argumentsIter.next();
            if (argument instanceof TagArgument)
                comparator = ((TagArgument) argument).getTag();
        }
        if (!":is".equals(comparator))
            throw new SyntaxException("Expecting \":is\"");

        // Second argument MUST be a list of day of week indices; 0=Sunday, 6=Saturday
        Set<Integer> daysToCheckAgainst = new HashSet<Integer>();
        if (argumentsIter.hasNext()) {
            Object argument = argumentsIter.next();
            if (argument instanceof StringListArgument) {
                List<String> valList = ((StringListArgument) argument).getList();
                for (String val : valList) {
                    int day;
                    try {
                        day = Integer.valueOf(val);
                    } catch (NumberFormatException e) {
                        throw new SyntaxException(e);
                    }
                    if (day < 0 || day > 6)
                        throw new SyntaxException("Expected values between 0 - 6");
                    // In Java 1=Sunday, 7=Saturday
                    daysToCheckAgainst.add(day + 1);
                }
            }
        }
        if (daysToCheckAgainst.isEmpty())
            throw new SyntaxException("Expecting at least one value");

        // There MUST NOT be any further arguments
        if (argumentsIter.hasNext())
            throw new SyntaxException("Found unexpected argument(s)");

        TimeZone accountTimeZone;
        try {
            accountTimeZone = Util.getAccountTimeZone(((ZimbraMailAdapter) mail).getMailbox().getAccount());
        } catch (ServiceException e) {
            throw new ZimbraSieveException(e);
        }
        Calendar rightNow = Calendar.getInstance(accountTimeZone);

        return daysToCheckAgainst.contains(rightNow.get(Calendar.DAY_OF_WEEK));
    }

    @Override
    protected void validateArguments(Arguments arguments, SieveContext context) {
        // override validation -- it's already done in executeBasic above
    }
}
