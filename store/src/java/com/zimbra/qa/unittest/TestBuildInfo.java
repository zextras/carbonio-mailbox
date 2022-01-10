// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import org.junit.*;
import static org.junit.Assert.*;

import com.zimbra.cs.account.AttributeManager;


public class TestBuildInfo  {

    @Test
    public void testInVersion() throws Exception {
        AttributeManager am = AttributeManager.getInstance();

        assertTrue(am.inVersion("zimbraId", "0"));
        assertTrue(am.inVersion("zimbraId", "5.0.10"));

        assertFalse(am.inVersion("zimbraZimletDomainAvailableZimlets", "5.0.9"));
        assertTrue(am.inVersion("zimbraZimletDomainAvailableZimlets", "5.0.10"));
        assertTrue(am.inVersion("zimbraZimletDomainAvailableZimlets", "5.0.11"));
        assertTrue(am.inVersion("zimbraZimletDomainAvailableZimlets", "5.5"));
        assertTrue(am.inVersion("zimbraZimletDomainAvailableZimlets", "6"));
    }

}
