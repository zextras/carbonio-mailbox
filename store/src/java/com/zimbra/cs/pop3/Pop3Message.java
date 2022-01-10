// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pop3;

import com.zimbra.cs.mailbox.Message;

/**
 * one for each message in the mailbox
 *
 * @since Nov 26, 2004
 * @author schemers
 */
public final class Pop3Message {
    private boolean retrieved = false;
    private boolean deleted = false;
    private int id;
    private long size; // raw size from blob store
    private String digest;

    /**
     * save enough info from the Message so we don't have to keep a reference to it.
     */
    public Pop3Message(Message msg) {
        this(msg.getId(), msg.getSize(), msg.getDigest());
    }

    public Pop3Message(int id, long size, String digest) {
        this.id = id;
        this.size = size;
        this.digest = digest;
    }

    long getSize() {
        return size;
    }

    int getId() {
        return id;
    }

    void setRetrieved(boolean value) {
        retrieved = value;
    }

    boolean isRetrieved() {
        return retrieved;
    }

    void setDeleted(boolean value) {
        deleted = value;
    }

    boolean isDeleted() {
        return deleted;
    }

    String getDigest() {
        return digest;
    }
}
