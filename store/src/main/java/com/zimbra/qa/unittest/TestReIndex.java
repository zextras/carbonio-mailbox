// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.account.soap.SoapProvisioning.ReIndexInfo;

/**
 * Unit test for ReIndex admin operation.
 * <p>
 * This test requires a Zimbra dev server instance.
 *
 * TODO: Add this class to {@link ZimbraSuite} once it supports JUnit 4
 * annotations.
 *
 * @author ysasaki
 */
public class TestReIndex {

    @BeforeClass
    public static void init() throws Exception {
        TestUtil.cliSetup();
    }

    @Test
    public void statusIdle() throws Exception {
        Account account = TestUtil.getAccount("user1");
        SoapProvisioning prov = TestProvisioningUtil.getSoapProvisioning();
        ReIndexInfo info = prov.reIndex(account, "status", null, null);
        Assert.assertEquals("idle", info.getStatus());
    }

}
