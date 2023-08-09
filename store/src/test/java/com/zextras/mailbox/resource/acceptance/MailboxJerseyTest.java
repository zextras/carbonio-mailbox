// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.acceptance;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/** Base class to run Jersey test using Junit5. */
public abstract class MailboxJerseyTest extends JerseyTest {

  // do not name this setup()
  @BeforeEach
  public void before() throws Exception {
    super.setUp();
  }

  // do not name this tearDown()
  @AfterEach
  public void after() throws Exception {
    super.tearDown();
  }
}
