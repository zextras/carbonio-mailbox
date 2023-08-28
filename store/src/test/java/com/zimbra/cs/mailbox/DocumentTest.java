// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/** Unit test for {@link Document}. */
public final class DocumentTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }
}
