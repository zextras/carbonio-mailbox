// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import com.zimbra.cs.mailbox.Mailbox;

/** This class represents blob data that has been "staged" to a place
 *  appropriate for the <code>Mailbox</code> it is in the process of
 *  being added to.  Data is first added to the local filesystem as a
 *  <code>Blob</code> via {@link StoreManager#storeIncoming}, then
 *  staged to the appropriate location as a <code>StagedBlob</code>
 *  via {@link StoreManager#stage}, and then placed in the correct
 *  permanent location as a <code>MailboxBlob</code> via either
 *  {@link StoreManager#link} or {@link StoreManager#renameTo}.<p>
 *  
 *  Note that in the default <code>FileBlobStore</code> case, 
 *  {@link StoreManager#stage} is a no-op and a <code>StagedBlob</code>
 *  is effectively just a wrapper around a <code>Blob</code>.  This
 *  is not always the case, however; {@link StoreManager#stage} may
 *  involve making a local copy of the original <code>Blob</code> or
 *  even pushing it out into the cloud. */
public abstract class StagedBlob {
    private final Mailbox mMailbox;
    private final String mDigest;
    private final long mSize;

    protected StagedBlob(Mailbox mbox, String digest, long size) {
        mMailbox = mbox;
        mDigest = digest;
        mSize = size;
    }

    public Mailbox getMailbox() {
        return mMailbox;
    }

    /** Returns the logical size of the blob after it was staged. */
    public long getSize() {
        return mSize;
    }

    /** Returns the digest of the blob after it was staged. */
    public String getDigest() {
        return mDigest;
    }

    public abstract String getLocator();
}
