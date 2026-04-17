// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import com.zimbra.cs.store.file.FileBlobStore;

public class FileBlobStoreTest extends AbstractStoreManagerTest {

    @Override
    protected StoreManager getStoreManager() {
        return new FileBlobStore();
    }
}
