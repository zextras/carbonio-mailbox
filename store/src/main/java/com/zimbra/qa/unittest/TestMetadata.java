// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Metadata;

public class TestMetadata {

    @Rule
    public TestName testInfo = new TestName();
    private static String USER_NAME = null;
    private static final String METADATA_SECTION = TestMetadata.class.getSimpleName();

    @Before
    public void setUp()
    throws Exception {
        USER_NAME = testInfo.getMethodName();
        cleanUp();
    }

    @After
    public void cleanUp()
    throws Exception {
        TestUtil.deleteAccountIfExists(USER_NAME);
    }

    /**
     * Tests insert, update and delete operations for mailbox metadata.
     */
    @Test
    public void insertUpdateDelete()
    throws Exception {
        TestUtil.createAccount(USER_NAME);
        Mailbox mbox = TestUtil.getMailbox(USER_NAME);
        assertNull("No metadata section should exist at start", mbox.getConfig(null, METADATA_SECTION));

        // Insert
        Metadata config = new Metadata();
        config.put("string", "mystring");
        mbox.setConfig(null, METADATA_SECTION, config);
        config = mbox.getConfig(null, METADATA_SECTION);
        assertEquals("Expected value for requested key after insert and get",
                "mystring", config.get("string"));

        // Update
        config.put("long", 87);
        mbox.setConfig(null, METADATA_SECTION, config);
        config = mbox.getConfig(null, METADATA_SECTION);
        assertEquals("Expected value for requested key after update and get", 87, config.getLong("long"));
        assertEquals("Expected value for original requested key after update",
                "mystring", config.get("string"));

        // Delete
        mbox.setConfig(null, METADATA_SECTION, null);
        assertNull("No metadata section should exist after it has been deleted",
                mbox.getConfig(null, METADATA_SECTION));
    }
}
