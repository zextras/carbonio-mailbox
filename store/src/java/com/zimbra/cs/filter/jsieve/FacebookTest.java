// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

import com.google.common.base.Strings;
import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.cs.mime.ParsedAddress;

/**
 * SIEVE test for Facebook notifications.
 * <p>
 * Built-in test for Facebook notifications excluding bulk messages:
 * <ul>
 *  <li>from {@code notification+*@facebookmail.com}
 * </ul>
 * Other Fecebook bulk messages appear to be the followings:
 * <ul>
 *  <li>from {@code deals+*@facebookmail.com}
 * </ul>
 *
 * @author ysasaki
 */
public final class FacebookTest extends AbstractTest {

    @Override
    protected boolean executeBasic(MailAdapter mail, Arguments args, SieveContext ctx) throws SieveException {
        if (mail instanceof DummyMailAdapter) {
            return true;
        }
        if (!(mail instanceof ZimbraMailAdapter)) {
            return false;
        }
        ZimbraMailAdapter adapter = (ZimbraMailAdapter) mail;

        ParsedAddress sender = adapter.getParsedMessage().getParsedSender();
        if (!Strings.isNullOrEmpty(sender.emailPart)) {
            String email = sender.emailPart.toLowerCase();
            if (email.endsWith("@facebookmail.com") && email.startsWith("notification+")) {
                return true;
            }
        }
        return false;
    }

}
