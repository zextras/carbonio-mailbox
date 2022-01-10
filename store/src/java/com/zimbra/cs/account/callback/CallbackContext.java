// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zimbra.cs.account.AttributeCallback;


public class CallbackContext {

    public enum Op {
        CREATE,
        MODIFY;
    };

    public enum DataKey {
        MAX_SIGNATURE_LEN,
        MAIL_FORWARDING_ADDRESS_MAX_LEN,
        MAIL_FORWARDING_ADDRESS_MAX_NUM_ADDRS,
        MAIL_WHITELIST_MAX_NUM_ENTRIES,
        MAIL_BLACKLIST_MAX_NUM_ENTRIES,
        PREV_EPHEMERAL_BACKEND_URL;
    };

    // whether the entry is being created
    private final Op op;

    // named of the entry being created
    private String creatingEntryName;

    // set of AttributeCallback marked themselves done
    private Set<Class<? extends AttributeCallback>> done = Sets.newHashSet();

    // data map
    private Map<DataKey, String> dataMap = Maps.newHashMap();

    public CallbackContext(Op op) {
        this.op = op;
    }

    public boolean isCreate() {
        return op == Op.CREATE;
    }

    public void setCreatingEntryName(String name) {
        creatingEntryName = name;
    }

    // name of the entry being creating
    public String getCreatingEntryName() {
        return creatingEntryName;
    }

    private void setDone(Class<? extends AttributeCallback> callback) {
        done.add(callback);
    }

    public boolean isDoneAndSetIfNot(Class<? extends AttributeCallback> callback) {
        boolean isDone = done.contains(callback);
        if (!isDone) {
            setDone(callback);
        }
        return isDone;
    }

    public void setData(DataKey key, String value) {
        dataMap.put(key, value);
    }

    public String getData(DataKey key) {
        return dataMap.get(key);
    }

}
