// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.octosync;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Document;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.ParsedDocument;
import java.io.InputStream;
import java.util.HashMap;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Unit test for {@link PatchInputStream}.
 *
 * @author grzes
 */
public final class PatchInputStreamTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  private Document addDocumentVersion(Mailbox mbox, String name, int itemId, InputStream in)
      throws Exception {
    ParsedDocument pd =
        new ParsedDocument(in, name, "text/plain", System.currentTimeMillis(), null, null);
    return mbox.addDocumentRevision(null, itemId, pd);
  }
}
