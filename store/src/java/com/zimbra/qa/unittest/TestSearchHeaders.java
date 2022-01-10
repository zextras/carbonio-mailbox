// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZSearchHit;
import com.zimbra.client.ZSearchParams;
import com.zimbra.client.ZSearchResult;
import com.zimbra.common.service.ServiceException;

public class TestSearchHeaders {
    @Rule
    public TestName testInfo = new TestName();

    private String USER_NAME = null;
    String id;

    @Before
    public void setUp() throws Exception {
        USER_NAME = testInfo.getMethodName();
        tearDown();
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.deleteAccountIfExists(USER_NAME);
    }

    private String getMimeString() {
        return "Subject: test\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/alternative; \n" +
                "    boundary=\"=_43a9c340-deb5-4ad3-a0a6-809c5d444d94\"\n" +
                "\n" +
                "--=_43a9c340-deb5-4ad3-a0a6-809c5d444d94\n" +
                "Content-Type: text/plain; charset=utf-8\n" +
                "Content-Transfer-Encoding: 8bit\n" +
                "\n" +
                "header search test\n" +
                "\n" +
                "--=_43a9c340-deb5-4ad3-a0a6-809c5d444d94--";
    }

    @Test
    public void searchNonTopLevelHeaders() throws ServiceException {
        TestUtil.createAccount(USER_NAME);
        ZMailbox mbox = TestUtil.getZMailbox(USER_NAME);
        id = mbox.addMessage("2",null, null, System.currentTimeMillis(), getMimeString(), false);
        ZSearchParams params = new ZSearchParams("\"header search test\" #Content-Transfer-Encoding:8bit");
        params.setTypes("message");
        ZSearchResult result = mbox.search(params);
        boolean found = false;
        for (ZSearchHit hit: result.getHits()) {
            if (found == false && hit.getId().equals(id)) {
                found = true;
            }
        }
        assertTrue(found);
    }
}
