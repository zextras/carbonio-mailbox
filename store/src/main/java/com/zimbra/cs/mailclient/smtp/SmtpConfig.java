// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.smtp;

import com.google.common.base.MoreObjects;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailclient.MailConfig;

/**
 * SMTP client configuration.
 */
public final class SmtpConfig extends MailConfig {
    public static final String PROTOCOL = "smtp";
    public static final int DEFAULT_PORT = 20025;
    public static final int DEFAULT_SSL_PORT = DEFAULT_PORT;
    public static final String DEFAULT_HOST = "127.78.0.7";

    private String domain;
    private boolean allowPartialSend;
    private String dsn;

    public SmtpConfig(String host, int port, String domain) {
        super(ZimbraLog.smtp, host);
        setPort(port);
        setDomain(domain);
    }

    public SmtpConfig(String host) {
        super(ZimbraLog.smtp, host);
        setPort(DEFAULT_PORT);
    }

    public SmtpConfig() {
        super(ZimbraLog.smtp);
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return MoreObjects.firstNonNull(domain, "localhost");
    }

    public void setAllowPartialSend(boolean allow) {
        this.allowPartialSend = allow;
    }

    public boolean isPartialSendAllowed() {
        return allowPartialSend;
    }

    public void setDsn(String dsn) {
        this.dsn = dsn;
    }

    public String getDsn() {
        return dsn;
    }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        helper
            .add("domain", domain)
            .add("allowPartialSend", allowPartialSend);
        if (null != dsn) {
            helper.add("dsn", dsn);
        }
        return helper;
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
