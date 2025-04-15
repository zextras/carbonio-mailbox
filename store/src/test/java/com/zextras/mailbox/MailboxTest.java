// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.MailboxTestData;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Provisioning;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.HashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MailboxTest {

  @TempDir
  public static File folder;

  private static final MailboxTestData testData = new MailboxTestData( "localhost", "test.com", "f4806430-b434-4e93-9357-a02d9dd796b8");


  @BeforeAll
  static void setUp() throws Exception {
    MailboxTestUtil.setUp(testData);
    Provisioning.getInstance().getLocalServer().modify(new HashMap<>(){{
      put(Provisioning.A_zimbraMailPort, "8080");
    }});
    final Path keystorePath = Path.of(folder.getAbsolutePath(), "keystore");
    KeyStore ks = KeyStore.getInstance("PKCS12");
    final String password = "test";
    ks.load(null, password.toCharArray());
    ks.store(Files.newOutputStream(keystorePath), password.toCharArray());

    LC.mailboxd_keystore.setDefault(keystorePath.toAbsolutePath().toString());
    LC.mailboxd_keystore_password.setDefault(password);
    LC.mailboxd_truststore_password.setDefault(password);
  }

  @AfterAll
  static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldNotStartMailboxIfDryRun() {
    assertDoesNotThrow(() -> Mailbox.main(new String[]{"-dryRun", "true"}));
  }

}