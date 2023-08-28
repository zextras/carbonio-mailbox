// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.ParsedDocument;

public class RenameTest {
    private Mailbox mbox;
    private Folder folder;
    private MailItem doc;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
        mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
        folder = mbox.createFolder(null, "/Briefcase/f", new Folder.FolderOptions().setDefaultView(MailItem.Type.DOCUMENT));
        InputStream in = new ByteArrayInputStream("This is a document".getBytes());
        ParsedDocument pd = new ParsedDocument(in, "doc.txt", "text/plain", System.currentTimeMillis(), null, null);
        doc = mbox.createDocument(null, folder.getId(), pd, MailItem.Type.DOCUMENT, 0);
    }

 @Test
 void renameModContentTest() throws Exception {
  int id = doc.getId();
  int mod_content = doc.getSavedSequence();
  mbox.rename(null, id, doc.getType(), "newdoc.txt", folder.getId());
  mbox.purge(MailItem.Type.UNKNOWN);
  MailItem newdoc = mbox.getItemById(null, id, MailItem.Type.UNKNOWN, false);
  assertEquals(mod_content, newdoc.getSavedSequence());
 }
}
