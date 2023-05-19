// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jsieve.Arguments;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.tests.AbstractTest;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.zimbra.common.mime.InternetAddress;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.filter.DummyMailAdapter;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import com.zimbra.cs.util.AccountUtil;

/**
 * SIEVE test whether or not the message is a bulk mail (legitimate mass marketing mail).
 *
 * @author ysasaki
 */
public final class BulkTest extends AbstractTest {
    private static final Set<String> HEADERS = ImmutableSet.of("X-ANTIABUSE", "X-REPORT-ABUSE", "X-ABUSE-REPORTS-TO",
            "X-MAIL_ABUSE_INQUIRES", "X-JOB", "UNSUBSCRIBE", "REMOVEEMAIL");
    private static final String LIST_UNSUBSCRIBE = "LIST-UNSUBSCRIBE";
    private static final String PRECEDENCE = "PRECEDENCE";
    private static final String X_PROOFPOINT_SPAM_DETAILS = "X-PROOFPOINT-SPAM-DETAILS";
    private static final String AUTO_SUBMITTED = "Auto-Submitted";
    private static final String ZIMBRA_OOO_AUTO_REPLY = "auto-replied (zimbra; vacation)";

    @Override
    protected boolean executeBasic(MailAdapter mail, Arguments args, SieveContext ctx) throws SieveException {
        if (mail instanceof DummyMailAdapter) {
            return true;
        }
        if (!(mail instanceof ZimbraMailAdapter)) {
            return false;
        }
        ZimbraMailAdapter adapter = (ZimbraMailAdapter) mail;
        for (String name : adapter.getHeaderNames()) {
            name = name.toUpperCase(); // compare in all upper-case
            if (HEADERS.contains(name)) { // test common bulk headers
                return true;
            } else if (LIST_UNSUBSCRIBE.equals(name)) { // test List-Unsubscribe
                // Check "to me" to avoid conflicting with legitimate mailing list messages
                List<InternetAddress> addrs = new ArrayList<>();
                for (String to : mail.getHeader("To")) {
                    addrs.addAll(InternetAddress.parseHeader(to));
                }
                try {
                    Set<String> me = AccountUtil.getEmailAddresses(adapter.getMailbox().getAccount());
                    for (InternetAddress addr : addrs) {
                        if (me.contains(addr.getAddress().toLowerCase())) {
                            return true;
                        }
                    }
                } catch (ServiceException e) {
                    ZimbraLog.filter.error("Failed to lookup my addresses", e);
                }
            } else if (PRECEDENCE.equals(name)) { // test "Precedence: bulk"
                for (String precedence : adapter.getHeader(PRECEDENCE)) {
                    if ("bulk".equalsIgnoreCase(precedence)) {
                        boolean zimbraOOONotif = false;
                        for (String autoSubmitted : mail.getHeader(AUTO_SUBMITTED)) {
                            if (ZIMBRA_OOO_AUTO_REPLY.equals(autoSubmitted)) {
                                zimbraOOONotif = true;
                                break;
                            }
                        }
                        if (!zimbraOOONotif) {
                            return true;
                        }
                    }
                }
            } else if (name.contains("CAMPAIGN")) { // test *CAMPAIGN*
                return true;
            } else if (name.equals(X_PROOFPOINT_SPAM_DETAILS)) { // test Proofpoint bulkscore > 50
                for (String value : adapter.getHeader(X_PROOFPOINT_SPAM_DETAILS)) {
                    for (String field : Splitter.on(' ').split(value)) {
                        if (field.startsWith("bulkscore=")) {
                            String[] pair = field.split("=", 2);
                            try {
                                if (pair.length == 2 && Integer.parseInt(pair[1]) >= 50) {
                                    return true;
                                }
                            } catch (NumberFormatException ignore) {
                            }
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }

}
