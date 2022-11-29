// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.client.ZEmailAddress;
import com.zimbra.client.ZFolder;
import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZMailbox.ZOutgoingMessage;
import com.zimbra.client.ZMailbox.ZOutgoingMessage.MessagePart;
import com.zimbra.client.ZMessage;
import com.zimbra.client.ZMessageHit;
import com.zimbra.client.ZSearchHit;
import com.zimbra.client.ZSearchParams;
import com.zimbra.client.ZSearchResult;
import com.zimbra.common.service.ServiceException;
import com.zimbra.soap.type.SearchSortBy;

public class TestDraftCount {

    private static final String USER_NAME = "testuser123";
    private static ZMailbox mbox;
    private static final int numMsgs = 5;

    @BeforeClass
    public static void setUp() throws ServiceException{
        TestUtil.createAccount(USER_NAME);
        mbox = TestUtil.getZMailbox(USER_NAME);
    }

    @AfterClass
    public static void tearDown() throws ServiceException {
        TestUtil.deleteAccount(USER_NAME);
    }

    private void deleteFromQuery(String query) throws Exception {
        //delete all messages currently in drafts folder
        ZSearchParams params = new ZSearchParams(query);
        params.setTypes("message");
        params.setOffset(0);
        ZSearchResult results = mbox.search(params);
        for (ZSearchHit hit: results.getHits()) {
            ZMessageHit msg = (ZMessageHit) hit;
            mbox.deleteMessage(hit.getId());
        }

    }

    private void checkMessageCount(String query, int expected) throws Exception {
        ZSearchParams params = new ZSearchParams(query);
        params.setTypes("message");
        params.setOffset(0);
        params.setSortBy(SearchSortBy.rcptAsc);
        ZSearchResult results = mbox.search(params);
        assertEquals(expected, results.getHits().size());
        params.setSortBy(SearchSortBy.rcptDesc);
        results = mbox.search(params);
        assertEquals(expected, results.getHits().size());
    }

    @Test
    public void testAllDraftsHaveRecipients() throws Exception {
        for (int i = 0; i < numMsgs; i++) {
            ZOutgoingMessage msg = new ZOutgoingMessage();
            msg.setMessagePart(new MessagePart("text/plain","test body "+i));
            msg.setSubject("test message "+i);
            List<ZEmailAddress> addresses = new ArrayList<ZEmailAddress>();
            addresses.add(new ZEmailAddress("testuser"+i, null, null, "t"));
            msg.setAddresses(addresses);
            ZMessage saved = mbox.saveDraft(msg, null, "6");
        }
        checkMessageCount("in:drafts", numMsgs);
        deleteFromQuery("in:drafts");
    }

    @Test
    public void testSomeDraftsHaveRecipients() throws Exception {
        for (int i = 0; i < numMsgs; i++) {
            ZOutgoingMessage msg = new ZOutgoingMessage();
            msg.setMessagePart(new MessagePart("text/plain","test body "+i));
            msg.setSubject("test message "+i);
            List<ZEmailAddress> addresses = new ArrayList<ZEmailAddress>();
            if (i%2 == 0) {
                addresses.add(new ZEmailAddress("testuser"+i, null, null, "t"));
                msg.setAddresses(addresses);
            }
            ZMessage saved = mbox.saveDraft(msg, null, "6");
        }
        checkMessageCount("in:drafts", numMsgs);
        deleteFromQuery("in:drafts");
    }

    @Test
    public void testInboxCount() throws Exception {
        for (int i = 0; i < numMsgs; i++) {
            TestUtil.addMessage(mbox, "test content "+i,"2");
        }
        checkMessageCount("in:inbox", numMsgs);
        deleteFromQuery("in:inbox");
    }

    @Test
    public void testCustomFolderCount() throws Exception {
        String folderName = "Test Custom Folder";
        ZFolder folder = mbox.createFolder("1", folderName, ZFolder.View.message, null, null, null);
        for (int i = 0; i < numMsgs; i++) {
            TestUtil.addMessage(mbox, "test content "+i,folder.getId());
        }
        checkMessageCount("in:\""+folderName+"\"", numMsgs);
        mbox.deleteFolder(folder.getId());
    }

    @Test
    public void testMixedFolderCount() throws Exception {
        //some inbox messages
        for (int i = 0; i < numMsgs; i++) {
            TestUtil.addMessage(mbox, "test content "+i,"2");
        }
        //and some drafts, some of which have no recipients
        for (int i = 0; i < numMsgs; i++) {
            ZOutgoingMessage msg = new ZOutgoingMessage();
            msg.setMessagePart(new MessagePart("text/plain","test body "+i));
            msg.setSubject("test message "+i);
            List<ZEmailAddress> addresses = new ArrayList<ZEmailAddress>();
            if (i%2 == 0) {
                addresses.add(new ZEmailAddress("testuser"+i, null, null, "t"));
                msg.setAddresses(addresses);
            }
            ZMessage saved = mbox.saveDraft(msg, null, "6");
        }
        checkMessageCount("in:inbox OR in:drafts", numMsgs*2);
        deleteFromQuery("in:inbox OR in:drafts");
    }

}