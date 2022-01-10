// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.LookupException;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

public class RelationalTest extends AbstractTest {

    @Override
    protected boolean executeBasic(MailAdapter mail, Arguments arguments,
            SieveContext context) throws SieveException {
        throw new SyntaxException("Unexpected test 'relational'");
    }
}
