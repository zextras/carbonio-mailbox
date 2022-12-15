// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

/**
 * Unit test for {@link LuceneIndex}.
 */

public final class LuceneIndexTest extends AbstractIndexStoreTest {

    @Override
    protected String getIndexStoreFactory() {
        // Default for LC.zimbra_class_index_store_factory.value() is USUALLY this
        return "com.zimbra.cs.index.LuceneIndex$Factory";
    }
}
