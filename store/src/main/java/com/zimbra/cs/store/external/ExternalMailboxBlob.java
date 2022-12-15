// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import java.io.IOException;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StoreManager;

/**
 * MailboxBlob implementation which accesses ExternalStoreManager to retrieve blobs for local use
 */
public class ExternalMailboxBlob extends MailboxBlob {

    protected ExternalMailboxBlob(Mailbox mbox, int itemId, int revision, String locator) {
        super(mbox, itemId, revision, locator);
    }

    @Override
    public Blob getLocalBlob() throws IOException {
        ExternalStoreManager sm = (ExternalStoreManager) StoreManager.getInstance();
        Blob blob = sm.getLocalBlob(getMailbox(), getLocator());

        setSize(blob.getRawSize());
        if (digest != null) {
            setDigest(blob.getDigest());
        }
        return blob;
    }

    @Override
    public int hashCode() {
        return getLocator().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof ExternalMailboxBlob)) {
            return false;
        }
        return getLocator().equals(((ExternalMailboxBlob) other).getLocator());
    }

    /**
     * Test if a MailboxBlob is valid and exists in remote store.
     * Default implementation preemptively fetches the complete blob to cache.
     * However, implementors may override this to do a HEAD or similar 'exists' operation
     * @return true if the blob is valid (i.e. locator exists)
     */
    public boolean validateBlob() {
        try {
            getLocalBlob();
            return true;
        } catch (Exception e) {
            ZimbraLog.store.debug("Unable to validate blob [%s] due to exception", this, e);
            return false;
        }
    }

}
