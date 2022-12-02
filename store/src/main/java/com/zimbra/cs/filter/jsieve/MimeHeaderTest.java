// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import static com.zimbra.cs.filter.jsieve.ComparatorName.ASCII_NUMERIC_COMPARATOR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.Header;

import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;

/**
 * Acts just like the original header test, but tests the headers
 * of all MIME parts instead of just the top-level message.
 */
public class MimeHeaderTest extends Header {

    @SuppressWarnings("unchecked")
    @Override
    protected boolean match(MailAdapter mail, String comparator,
                            String matchType, List headerNames, List keys, SieveContext context)
    throws SieveException {
        if (mail instanceof DummyMailAdapter) {
            return true;
        }
        if (!(mail instanceof ZimbraMailAdapter)) {
            return false;
        }
        ZimbraMailAdapter zma = (ZimbraMailAdapter) mail;
        if (ASCII_NUMERIC_COMPARATOR.equalsIgnoreCase(comparator)) {
            Require.checkCapability(zma, ASCII_NUMERIC_COMPARATOR);
        }
        // Iterate over the header names looking for a match
        boolean isMatched = false;
        Iterator<String> headerNamesIter = headerNames.iterator();
        while (!isMatched && headerNamesIter.hasNext()) {
            Set<String> values = zma.getMatchingHeaderFromAllParts(headerNamesIter.next());
            isMatched = match(comparator, matchType, new ArrayList<String>(values), keys, context);
        }
        return isMatched;
    }
}
