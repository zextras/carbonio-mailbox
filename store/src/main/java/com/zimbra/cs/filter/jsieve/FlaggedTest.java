// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import java.util.Iterator;
import java.util.Set;

import org.apache.jsieve.Argument;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.Action;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

import com.google.common.collect.ImmutableSet;
import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.FilterUtil;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.cs.mailbox.Message;

/**
 * SIEVE test whether or not the message is flagged either by a SIEVE rule that was examined before the current SIEVE
 * rule or on the existing message being filtered.
 * <p>
 * For example, given a SIEVE rule set:
 * <pre>
 *  (1) if socialcast { flag "priority"; }
 *  (2) if me :in "To" { tag "me"; }
 *  (3) if flagged "priority" { stop; }
 *  (4) if bulk { fileinto "bulk"; }
 * <pre>
 * A message that matches (1) is still examined by (2). But, once the message is flagged as "priority" in (1), (4) is
 * not examined because (3) stops further processing.
 *
 * @author ysasaki
 */
public final class FlaggedTest extends AbstractTest {

    private Set<ActionFlag> flags;

    @Override
    protected void validateArguments(Arguments args, SieveContext ctx) throws SieveException {
        Iterator<Argument> itr = args.getArgumentList().iterator();
        if (itr.hasNext()) {
            Argument arg = itr.next();
            if (arg instanceof StringListArgument) {
                ImmutableSet.Builder<ActionFlag> builder = ImmutableSet.builder();
                for (String name : ((StringListArgument) arg).getList()) {
                    ActionFlag flag = ActionFlag.of(name);
                    if (flag != null) {
                        builder.add(flag);
                    } else {
                        throw ctx.getCoordinate().syntaxException("Invalid flag: " + name);
                    }
                }
                flags = builder.build();
            } else {
                throw ctx.getCoordinate().syntaxException("Unexpected argument: " + arg.getValue());
            }
        } else {
            throw ctx.getCoordinate().syntaxException("No flag names specified");
        }
    }

    @Override
    protected boolean executeBasic(MailAdapter mail, Arguments args, SieveContext ctx) throws SieveException {
        assert(flags != null);
        if (mail instanceof DummyMailAdapter) {
            return true;
        }
        if (!(mail instanceof ZimbraMailAdapter)) {
            return false;
        }
        ZimbraMailAdapter adapter = (ZimbraMailAdapter) mail;

        // check actions already taken by a previous filter
        for (Action action : adapter.getActions()) {
            if (action instanceof ActionFlag) {
                if (flags.contains(action)) {
                    return true;
                }
            }
        }

        // check the message's flags if this filter is running against the existing messages
        Message msg = adapter.getMessage();
        if (msg != null) {
            int bitmask = msg.getFlagBitmask();
            return bitmask == FilterUtil.getFlagBitmask(flags, bitmask);
        }

        return false;
    }
}
