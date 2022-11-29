// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import org.junit.After;

import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.file.BlobConsistencyChecker;

public class ValidationFailingBlobConsistencyCheckTest extends ExternalBlobConsistencyCheckTest {

    private MockValidationFailingStore storeManager = new MockValidationFailingStore();

    @Override
    protected StoreManager getStoreManager() {
        return storeManager;
    }

    @Override
    protected BlobConsistencyChecker getChecker() {
        //need to suppress validation errors until after test data is setup
        //so we set it here, and unset it in teardown
        //careful here if refactoring the tests; as order matters
        storeManager.setFailOnValidate(true);
        return super.getChecker();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        storeManager.setFailOnValidate(false);
        super.tearDown();
    }

}
