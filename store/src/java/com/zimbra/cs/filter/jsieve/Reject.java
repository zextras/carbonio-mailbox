// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.FilterUtil;
import com.zimbra.cs.filter.ZimbraMailAdapter;

import static com.zimbra.cs.filter.JsieveConfigMapHandler.CAPABILITY_REJECT;

import org.apache.jsieve.Arguments;
import org.apache.jsieve.Block;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.ActionKeep;
import org.apache.jsieve.mail.ActionReject;
import org.apache.jsieve.mail.MailAdapter;

/**
 * Class Reject implements the Reject Command as defined in RFC 5429,
 * section 2.2.
 */
public class Reject extends org.apache.jsieve.commands.optional.Reject {

    @Override
    protected Object executeBasic(MailAdapter mail, Arguments arguments, Block block, SieveContext context)
            throws SieveException {
        if (!(mail instanceof ZimbraMailAdapter)) {
            return null;
        }
        ZimbraMailAdapter mailAdapter = (ZimbraMailAdapter) mail;
        Require.checkCapability(mailAdapter, CAPABILITY_REJECT);

        Account account = null;
        account = mailAdapter.getAccount();
        if (account.isSieveRejectMailEnabled()) {
            mailAdapter.setDiscardActionPresent();
            final String message = FilterUtil.replaceVariables((ZimbraMailAdapter) mailAdapter,
                ((StringListArgument) arguments.getArgumentList().get(0)).getList().get(0));
            mail.addAction(new ActionReject(message));
        } else {
            mail.addAction(new ActionKeep());
        }
        return null;
    }
}
