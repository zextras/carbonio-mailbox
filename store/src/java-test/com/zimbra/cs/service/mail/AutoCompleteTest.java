// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestName;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.util.ZTestWatchman;

import junit.framework.Assert;

public class AutoCompleteTest {

    @Rule
    public TestName testName = new TestName();
    @Rule
    public MethodRule watchman = new ZTestWatchman();

    @Before
    public void setUp() throws Exception {
        System.out.println(testName.getMethodName());
        MailboxTestUtil.initServer();
        MailboxTestUtil.clearData();
        Provisioning prov = Provisioning.getInstance();
        Map<String, Object> attrs = Maps.newHashMap();
        prov.createDomain("zimbra.com", attrs);

        attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        prov.createAccount("test3951@zimbra.com", "secret", attrs);
    }

    @Test
    public void test3951() throws Exception {
        Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test3951@zimbra.com");
        Element request = new Element.XMLElement(MailConstants.AUTO_COMPLETE_REQUEST);
        request.addAttribute("name", " ");
        boolean exceptionThrown;
        try {
            new AutoComplete().handle(request, ServiceTestUtil.getRequestContext(acct));
            exceptionThrown = false;
        } catch (ServiceException e) {
            exceptionThrown = true;
            Assert.assertEquals("invalid request: name parameter is empty", e.getMessage());
        }
        Assert.assertEquals(true, exceptionThrown);
    }

    @After
    public void tearDown() {
        try {
            MailboxTestUtil.clearData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
