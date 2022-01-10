// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.listeners;

import com.zimbra.cs.listeners.ListenerUtil.Priority;

public class AccountListenerEntry implements Comparable<AccountListenerEntry> {

    private String listenerName;
    private Priority priority;
    private AccountListener accountListener;

    public AccountListenerEntry(String listenerName, Priority priority, AccountListener accountListener) {
        this.listenerName = listenerName;
        this.priority = priority;
        this.accountListener = accountListener;
    }

    @Override
    public int compareTo(AccountListenerEntry other) {
        if (this.priority.ordinal() < other.priority.ordinal())
            return -1;
        else if (this.priority.ordinal() > other.priority.ordinal())
            return 1;
        else
            return 0;
    }

    public AccountListener getAccountListener() {
        return accountListener;
    }

    public void setAccountListener(AccountListener accountListener) {
        this.accountListener = accountListener;
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

}

