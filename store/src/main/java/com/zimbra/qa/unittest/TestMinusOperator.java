// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZMailbox.ZOutgoingMessage;
import com.zimbra.client.ZSearchParams;
import com.zimbra.client.ZSearchResult;
import com.zimbra.common.service.ServiceException;

public class TestMinusOperator {

    private static final String USER_NAME = "testuser123";
    private static final String REMOTE_USER_NAME = "testuser456";
    private static ZMailbox mbox;

    @Before
    public void setUp() throws ServiceException{
        cleanup();
        TestUtil.createAccount(USER_NAME);
        TestUtil.createAccount(REMOTE_USER_NAME);
        mbox = TestUtil.getZMailbox(USER_NAME);
        ZOutgoingMessage msg = TestUtil.getOutgoingMessage(REMOTE_USER_NAME, "test message", "far over the misty mountains cold",null);
        mbox.sendMessage(msg,null,false);
    }

    @After
    public void tearDown() throws ServiceException {
        cleanup();
    }

    private void cleanup() throws ServiceException {
        if(TestUtil.accountExists(USER_NAME)) {
            TestUtil.deleteAccount(USER_NAME);
        }
        if(TestUtil.accountExists(REMOTE_USER_NAME)) {
            TestUtil.deleteAccount(REMOTE_USER_NAME);
        }
    }

    @Test
    public void testExcludeText() throws ServiceException {
        ZSearchResult search1 = mbox.search(new ZSearchParams("in:sent test"));
        Assert.assertEquals(search1.getHits().size(),1); //control
        ZSearchResult search2 = mbox.search(new ZSearchParams("in:sent -test"));
        Assert.assertEquals(search2.getHits().size(),0);
    }

    @Test
    public void testExcludeRecipient() throws ServiceException {
        ZSearchResult search1 = mbox.search(new ZSearchParams("in:sent to:"+REMOTE_USER_NAME));
        Assert.assertEquals(search1.getHits().size(),1);
        ZSearchResult search2 = mbox.search(new ZSearchParams("in:sent -to:"+REMOTE_USER_NAME));
        Assert.assertEquals(search2.getHits().size(),0);
    }

}
