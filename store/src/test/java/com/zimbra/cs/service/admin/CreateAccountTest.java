// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateAccountTest {

  private static Provisioning provisioning;

  @BeforeAll
  static void setUp() throws Exception {
    MailboxTestUtil.initServer();
    provisioning = Provisioning.getInstance();
  }

  @Test
  void shouldFailWhenMissingSmtpTransportAndMailHost() {
    assertThrows(
        Exception.class,
        () -> provisioning.createAccount("test@demo.com", "password", new HashMap<>()));
  }
}
