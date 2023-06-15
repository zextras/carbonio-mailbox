// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;

public class MailboxBlobTest {
 @Test
 void serialization() throws Exception {
  MailboxBlobInfo mbinfo = new MailboxBlobInfo(MockProvisioning.DEFAULT_ACCOUNT_ID, 1, Mailbox.FIRST_USER_ID, 1, "locator", "digest123");

  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  ObjectOutputStream oos = new ObjectOutputStream(baos);
  oos.writeObject(mbinfo);
  oos.close();

  ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
  MailboxBlobInfo mbi2 = (MailboxBlobInfo) ois.readObject();
  assertEquals(mbinfo.accountId, mbi2.accountId);
  assertEquals(mbinfo.mailboxId, mbi2.mailboxId);
  assertEquals(mbinfo.itemId, mbi2.itemId);
  assertEquals(mbinfo.revision, mbi2.revision);
  assertEquals(mbinfo.locator, mbi2.locator);
  assertEquals(mbinfo.digest, mbi2.digest);
 }
}
