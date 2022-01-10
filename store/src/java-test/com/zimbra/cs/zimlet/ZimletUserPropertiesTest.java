// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link ZimletUserProperties}
 *
 * @author ysasaki
 */
public final class ZimletUserPropertiesTest {
    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @Before
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

    @Test
    public void save() throws Exception {
        Provisioning prov = Provisioning.getInstance();
        Account account = prov.getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
        ZimletUserProperties prop = ZimletUserProperties.getProperties(account);
        prop.setProperty("phone", "123123", "aaaaaaaaaaaa");
        prop.setProperty("phone", "number", "bar");
        prop.saveProperties(account);

        String[] values = account.getZimletUserProperties();
        Arrays.sort(values);
        Assert.assertArrayEquals(new String[] {"phone:123123:aaaaaaaaaaaa", "phone:number:bar"}, values);
    }
}
