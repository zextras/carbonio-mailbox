// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;

public class MailboxBlobTest {
    @Test
    public void serialization() throws Exception {
        MailboxBlobInfo mbinfo = new MailboxBlobInfo(MockProvisioning.DEFAULT_ACCOUNT_ID, 1, Mailbox.FIRST_USER_ID, 1, "locator", "digest123");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mbinfo);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        MailboxBlobInfo mbi2 = (MailboxBlobInfo) ois.readObject();
        Assert.assertEquals(mbinfo.accountId, mbi2.accountId);
        Assert.assertEquals(mbinfo.mailboxId, mbi2.mailboxId);
        Assert.assertEquals(mbinfo.itemId, mbi2.itemId);
        Assert.assertEquals(mbinfo.revision, mbi2.revision);
        Assert.assertEquals(mbinfo.locator, mbi2.locator);
        Assert.assertEquals(mbinfo.digest, mbi2.digest);
    }
}
