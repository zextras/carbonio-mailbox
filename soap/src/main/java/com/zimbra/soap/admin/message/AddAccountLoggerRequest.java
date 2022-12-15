// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.LoggerInfo;
import com.zimbra.soap.type.AccountSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Changes logging settings on a per-account basis<br />
 * Adds a custom logger for the given account and log category.  The logger stays in effect only during the
 * lifetime of the current server instance.  If the request is sent to a server other than the one that the account
 * resides on, it is proxied to the correct server.
 * <p>
 * If the category is "all", adds a custom logger for every category or the given user.
 * </p>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_ADD_ACCOUNT_LOGGER_REQUEST)
public class AddAccountLoggerRequest {

    /**
     * @zm-api-field-description <b>Deprecated</b> - use <b>&lt;account></b> instead
     */
    @XmlElement(name=AdminConstants.E_ID /* id */, required=false)
    @Deprecated
    private String id;

    /**
     * @zm-api-field-description Use to select account
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT /* account */, required=false)
    private AccountSelector account;

    /**
     * @zm-api-field-description Logger category
     */
    @XmlElement(name=AdminConstants.E_LOGGER /* logger */, required=true)
    private LoggerInfo logger;

    private AddAccountLoggerRequest() {
        this((AccountSelector) null, (LoggerInfo) null);
    }

    private AddAccountLoggerRequest(AccountSelector account, LoggerInfo logger) {
        setAccount(account);
        setLogger(logger);
    }

    public static AddAccountLoggerRequest createForAccountAndLogger(AccountSelector account, LoggerInfo logger) {
        return new AddAccountLoggerRequest(account, logger);
    }

    @Deprecated
    public void setId(String id) { this.id = id; }
    public void setAccount(AccountSelector account) { this.account = account; }
    public void setLogger(LoggerInfo logger) { this.logger = logger; }
    @Deprecated
    public String getId() { return id; }
    public AccountSelector getAccount() { return account; }
    public LoggerInfo getLogger() { return logger; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("account", account)
            .add("logger", logger);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
