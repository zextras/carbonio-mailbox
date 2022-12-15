// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.listeners;

import com.zimbra.cs.listeners.ListenerUtil.Priority;

public class AuthListenerEntry implements Comparable<AuthListenerEntry> {

    private String listenerName;
    private Priority priority;
    private AuthListener authListener;

    public AuthListenerEntry(String listenerName, Priority priority, AuthListener authListener) {
        this.listenerName = listenerName;
        this.priority = priority;
        this.authListener = authListener;
    }

    @Override
    public int compareTo(AuthListenerEntry other) {
        if (this.priority.ordinal() < other.priority.ordinal())
            return -1;
        else if (this.priority.ordinal() > other.priority.ordinal())
            return 1;
        else
            return 0;
    }

    public AuthListener getAuthListener() {
        return authListener;
    }

    public void setAuthListener(AuthListener authListener) {
        this.authListener = authListener;
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

}

