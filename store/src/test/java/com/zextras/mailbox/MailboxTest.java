// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.localconfig.LC;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MailboxTest {

  @TempDir
  public static File folder;

  @BeforeAll
  static void setUp() throws Exception {
    MailboxTestUtil.setUp();
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
  void shouldNotStartMailboxIfDryRun() throws Exception {
    final String webApp = new File("conf/").getAbsolutePath();
    final String webDescriptor = new File("conf/web-dev.xml").getAbsolutePath();
    Mailbox.main(new String[]{"-webDescriptor", webDescriptor, "-webApp", webApp, "-dryRun", "true"});
  }

}