// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 10. 12.
 */
package com.zimbra.cs.store;

import java.io.IOException;
import java.io.Serializable;

import com.zimbra.cs.mailbox.Mailbox;

public abstract class MailboxBlob {
    public static class MailboxBlobInfo implements Serializable {
        private static final long serialVersionUID = 6378518636677970767L;

        public String accountId;
        public int mailboxId;
        public int itemId;
        public int revision;
        public String locator;
        public String digest;

        public MailboxBlobInfo(String accountId, int mailboxId, int itemId, int revision, String locator, String digest) {
            this.accountId = accountId;
            this.mailboxId = mailboxId;
            this.itemId = itemId;
            this.revision = revision;
            this.locator = locator;
            this.digest = digest;
        }
    }

    private final Mailbox mailbox;

    private final int itemId;
    private final int revision;
    private final String locator;
    protected Long size;
    protected String digest;

    protected MailboxBlob(Mailbox mbox, int itemId, int revision, String locator) {
        this.mailbox = mbox;
        this.itemId = itemId;
        this.revision = revision;
        this.locator = locator;
    }

    public int getItemId() {
        return itemId;
    }

    public int getRevision() {
        return revision;
    }

    public String getLocator() {
        return locator;
    }

    public String getDigest() throws IOException {
        if (digest == null) {
            digest = getLocalBlob().getDigest();
        }
        return digest;
    }

    public MailboxBlob setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public long getSize() throws IOException {
        if (size == null) {
            this.size = Long.valueOf(getLocalBlob().getRawSize());
        }
        return size;
    }

    public MailboxBlob setSize(long size) {
        this.size = size;
        return this;
    }

    public Mailbox getMailbox() {
        return mailbox;
    }

    abstract public Blob getLocalBlob() throws IOException;

    @Override
    public String toString() {
        return mailbox.getId() + ":" + itemId + ":" + revision + "[" + getLocator() + "]";
    }
}
