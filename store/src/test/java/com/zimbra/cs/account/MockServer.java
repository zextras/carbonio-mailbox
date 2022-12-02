// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.HashMap;

public class MockServer extends Server {

  public MockServer(String name, String id) {
    super(
        name,
        id,
        new HashMap<String, Object>(),
        new HashMap<String, Object>(),
        new MockProvisioning());
  }
}
