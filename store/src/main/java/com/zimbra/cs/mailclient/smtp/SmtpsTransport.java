// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.smtp;

import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

import com.zimbra.cs.util.BuildInfo;

/**
 * A custom SMTPS (SMTP over SSL) {@link Transport} implementation using
 * {@link SmtpConnection}.
 *
 * @see SmtpTransport
 * @author ysasaki
 */
public final class SmtpsTransport extends SmtpTransport {

    public static final Provider PROVIDER = new Provider(
            Provider.Type.TRANSPORT, "smtps", SmtpsTransport.class.getName(),
            "Zimbra", BuildInfo.VERSION);

    public SmtpsTransport(Session session, URLName urlname) {
        super(session, urlname, true);
    }

}
