// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.zimbra.cs.store.StoreManager;

public class SimpleStoreManagerTest extends AbstractExternalStoreManagerTest {

  @Override
  protected StoreManager getStoreManager() {
    return new SimpleStoreManager();
  }
}
