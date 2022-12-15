// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.StagedBlob;

public class ExternalStagedBlob extends StagedBlob {

    private final String locator;
    private boolean inserted;

    public ExternalStagedBlob(Mailbox mbox, String digest, long size, String locator) {
        super(mbox, digest, size);
        this.locator = locator;
    }

    @Override
    public String getLocator() {
        return locator;
    }

    ExternalStagedBlob markInserted() {
        inserted = true;
        return this;
    }

    boolean isInserted() {
        return inserted;
    }
}
