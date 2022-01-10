// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob;

public class MockValidationFailingStore extends SimpleStoreManager {

    private boolean failOnValidate = false;

    @Override
    public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId,
            int revision, String locator, boolean validate)
            throws ServiceException {
        if (!validate || !failOnValidate) {
            return super.getMailboxBlob(mbox, itemId, revision, locator, false);
        } else {
            return null;
        }
    }

    public void setFailOnValidate(boolean failOnValidate) {
        this.failOnValidate = failOnValidate;
    }
}
