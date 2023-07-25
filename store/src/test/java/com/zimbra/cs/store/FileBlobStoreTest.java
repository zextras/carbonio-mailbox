// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import com.zimbra.cs.store.file.FileBlobStore;
import org.junit.jupiter.api.BeforeAll;

public class FileBlobStoreTest extends AbstractStoreManagerTest {

    @BeforeAll
    public static void disableNative() {
        //don't fail test even if native libraries not installed
        //this makes it easier to run unit tests from command line
        System.setProperty("zimbra.native.required", "false");
    }

    @Override
    protected StoreManager getStoreManager() {
        return new FileBlobStore();
    }
}
