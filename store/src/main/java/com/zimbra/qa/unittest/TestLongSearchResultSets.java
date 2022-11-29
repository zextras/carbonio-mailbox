// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZSearchParams;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.soap.type.SearchSortBy;

public class TestLongSearchResultSets extends TestCase {
    private static final String NAME_PREFIX = TestLongSearchResultSets.class.getSimpleName();
    private static final String RECIPIENT = "search_test_1";
    private static final String RECIPIENT2 = "search_test_2";
    private static final String SENDER = "user1";
    private static final String PASSWORD = "test123";
    private static final String SEARCH_STRING = "jkfheowerklsaklasdqweds";

    @Override
    public void setUp() throws Exception {
        cleanUp();

        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("zimbraMailHost", LC.zimbra_server_hostname.value());
        attrs.put("zimbraAttachmentsIndexingEnabled", true);
        attrs.put("cn", "search_test1");
        attrs.put("displayName", "TestAccount unit test user 1");
        Provisioning.getInstance().createAccount(TestUtil.getAddress(RECIPIENT), PASSWORD, attrs);

        attrs = new HashMap<String, Object>();
        attrs.put("zimbraMailHost", LC.zimbra_server_hostname.value());
        attrs.put("zimbraAttachmentsIndexingEnabled", false);
        attrs.put("cn", "search_tes2t");
        attrs.put("displayName", "TestAccount unit test user 2");
        Provisioning.getInstance().createAccount(TestUtil.getAddress(RECIPIENT2), PASSWORD, attrs);
    }
    @Test
    public void testConversations()  {
        try {
            Mailbox recieverMbox = TestUtil.getMailbox(RECIPIENT);
            long timestamp = System.currentTimeMillis() - 3600*60*1000;
            int numMessages = 3207;
            int firstLimit = 100;
            int incLimit = 50;
            int offset = 0;
            ArrayList<String> expectedIds = new ArrayList<String>();
            for(int i=0;i<numMessages;i++) {
                Message msg = TestUtil.addMessage(recieverMbox, TestUtil.getAddress(RECIPIENT), TestUtil.getAddress(SENDER), NAME_PREFIX + " testing bug " + i, String.format("this message contains a search string %s which we are searching for and a number %d and timestamp %d", SEARCH_STRING, i,timestamp), timestamp);
                expectedIds.add(Integer.toString(msg.getId()));
                timestamp +=1000;
                Thread.sleep(100);
            }
            Collections.reverse(expectedIds);
            Thread.sleep(100);
            ZMailbox zmbx = TestUtil.getZMailbox(RECIPIENT);
            ZSearchParams searchParams = new ZSearchParams("in:inbox " + SEARCH_STRING);
            searchParams.setSortBy(SearchSortBy.dateDesc);
            searchParams.setLimit(firstLimit);
            searchParams.setTypes(ZSearchParams.TYPE_CONVERSATION);
            List<String> msgIds = TestUtil.searchMessageIds(zmbx, searchParams);
            assertEquals(firstLimit,msgIds.size());
            int gotMessages = msgIds.size();
            List<String> seenIds = new ArrayList<String>();
            seenIds.addAll(msgIds);
            int recCount = 1;
            offset+=firstLimit;
       //     int numDups = 0;
            while(gotMessages > 0) {
                searchParams = new ZSearchParams("in:inbox " + SEARCH_STRING);
                searchParams.setSortBy(SearchSortBy.dateDesc);
                searchParams.setLimit(incLimit);
                searchParams.setTypes(ZSearchParams.TYPE_CONVERSATION);
                searchParams.setOffset(offset);
                msgIds = TestUtil.searchMessageIds(zmbx, searchParams);
                recCount++;
                gotMessages = msgIds.size();
                int resCount = 0;
                for(String szId : msgIds) {
                    assertFalse(String.format("Request %d, result %d, encountered duplicate ID %s. Previously seen at %d", recCount, resCount, szId, seenIds.indexOf(szId)), seenIds.contains(szId));
                    seenIds.add(szId);
                    resCount++;
                }
                Thread.sleep(100); //jetty sometimes crashes on Mac when bombarded with request without a timeout
                offset+=incLimit;
            }
            assertEquals("Returned incorrect number of conversations", numMessages, seenIds.size());
        } catch (ServiceException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testConversations2()  {
        try {
            Mailbox recieverMbox = TestUtil.getMailbox(RECIPIENT2);
            long timestamp = System.currentTimeMillis() - 3600*60*1000;
            int numMessages = 3207;
            int firstLimit = 100;
            int incLimit = 50;
            int offset = 0;
            ArrayList<String> expectedIds = new ArrayList<String>();
            for(int i=0;i<numMessages;i++) {
                Message msg = TestUtil.addMessage(recieverMbox, TestUtil.getAddress(RECIPIENT2), TestUtil.getAddress(SENDER), NAME_PREFIX + " testing bug " + i, String.format("this message contains a search string %s which we are searching for and a number %d and timestamp %d", SEARCH_STRING, i,timestamp), timestamp);
                expectedIds.add(Integer.toString(msg.getId()));
                timestamp +=1000;
                Thread.sleep(100);
            }
            Collections.reverse(expectedIds);
            Thread.sleep(100);
            ZMailbox zmbx = TestUtil.getZMailbox(RECIPIENT2);
            ZSearchParams searchParams = new ZSearchParams("in:inbox " + SEARCH_STRING);
            searchParams.setSortBy(SearchSortBy.dateDesc);
            searchParams.setLimit(firstLimit);
            searchParams.setTypes(ZSearchParams.TYPE_CONVERSATION);
            List<String> msgIds = TestUtil.searchMessageIds(zmbx, searchParams);
            assertEquals(firstLimit,msgIds.size());
            int gotMessages = msgIds.size();
            List<String> seenIds = new ArrayList<String>();
            seenIds.addAll(msgIds);
            int recCount = 1;
            offset+=firstLimit;
            while(gotMessages > 0) {
                searchParams = new ZSearchParams("in:inbox " + SEARCH_STRING);
                searchParams.setSortBy(SearchSortBy.dateDesc);
                searchParams.setLimit(incLimit);
                searchParams.setTypes(ZSearchParams.TYPE_CONVERSATION);
                searchParams.setOffset(offset);
                msgIds = TestUtil.searchMessageIds(zmbx, searchParams);
                recCount++;
                gotMessages = msgIds.size();
                int resCount = 0;
                for(String szId : msgIds) {
                    assertFalse(String.format("Request %d, result %d, encountered duplicate ID %s. Previously seen at %d", recCount, resCount, szId, seenIds.indexOf(szId)), seenIds.contains(szId));
                    seenIds.add(szId);
                    resCount++;
                }
                Thread.sleep(100); //jetty sometimes crashes on Mac when bombarded with request without a timeout
                offset+=incLimit;
            }
            assertEquals("Returned incorrect number of conversations", numMessages, seenIds.size());
        } catch (ServiceException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testMessages()  {

        try {
            Mailbox recieverMbox = TestUtil.getMailbox(RECIPIENT);
            long timestamp = System.currentTimeMillis() - 3600*60*1000;
            int numMessages = 2257;
            int firstLimit = 100;
            int incLimit = 50;
            int offset = 0;
            ArrayList<String> expectedIds = new ArrayList<String>();
            for(int i=0;i<numMessages;i++) {
                Message msg = TestUtil.addMessage(recieverMbox, TestUtil.getAddress(RECIPIENT), TestUtil.getAddress(SENDER), NAME_PREFIX + " testing bug " + i, String.format("this message contains a search string %s which we are searching for and a number %d and timestamp %d", SEARCH_STRING, i,timestamp), timestamp);
                expectedIds.add(Integer.toString(msg.getId()));
                timestamp +=1000;
                Thread.sleep(100);
            }
            Collections.reverse(expectedIds);
            Thread.sleep(100);
            ZMailbox zmbx = TestUtil.getZMailbox(RECIPIENT);
            ZSearchParams searchParams = new ZSearchParams("in:inbox " + SEARCH_STRING);
            searchParams.setSortBy(SearchSortBy.dateDesc);
            searchParams.setLimit(firstLimit);
            searchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
            List<String> msgIds = TestUtil.searchMessageIds(zmbx, searchParams);
            assertEquals(firstLimit,msgIds.size());
            int gotMessages = msgIds.size();
            List<String> seenIds = new ArrayList<String>();
            seenIds.addAll(msgIds);
            int recCount = 1;
            offset+=firstLimit;
            while(gotMessages > 0) {
                searchParams = new ZSearchParams("in:inbox " + SEARCH_STRING);
                searchParams.setSortBy(SearchSortBy.dateDesc);
                searchParams.setLimit(incLimit);
                searchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
                searchParams.setOffset(offset);
                msgIds = TestUtil.searchMessageIds(zmbx, searchParams);
                recCount++;
                gotMessages = msgIds.size();
                int resCount = 0;
                for(String szId : msgIds) {
                    assertFalse(String.format("Request %d, result %d, encountered duplicate ID %s. Previously seen at %d", recCount, resCount, szId, seenIds.indexOf(szId)), seenIds.contains(szId));
                    seenIds.add(szId);
                    resCount++;
                }
                Thread.sleep(100); //jetty sometimes crashes on Mac when bombarded with request without a timeout
                offset+=incLimit;
            }
            for(int i=0;i<expectedIds.size();i++) {
               assertEquals("IDs at index " + i + " do not match", expectedIds.get(i),seenIds.get(i) );
            }
            assertEquals("Returned incorrect number of messages", numMessages, seenIds.size());
        } catch (ServiceException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testMessages2()  {

        try {
            Mailbox recieverMbox = TestUtil.getMailbox(RECIPIENT2);
            long timestamp = System.currentTimeMillis() - 3600*60*1000;
            int numMessages = 2257;
            int firstLimit = 100;
            int incLimit = 50;
            int offset = 0;
            ArrayList<String> expectedIds = new ArrayList<String>();
            for(int i=0;i<numMessages;i++) {
                Message msg = TestUtil.addMessage(recieverMbox, TestUtil.getAddress(RECIPIENT2), TestUtil.getAddress(SENDER), NAME_PREFIX + " testing bug " + i, String.format("this message contains a search string %s which we are searching for and a number %d and timestamp %d", SEARCH_STRING, i,timestamp), timestamp);
                expectedIds.add(Integer.toString(msg.getId()));
                timestamp +=1000;
                Thread.sleep(100);
            }
            Collections.reverse(expectedIds);
            Thread.sleep(100);
            ZMailbox zmbx = TestUtil.getZMailbox(RECIPIENT2);
            ZSearchParams searchParams = new ZSearchParams("in:inbox " + SEARCH_STRING);
            searchParams.setSortBy(SearchSortBy.dateDesc);
            searchParams.setLimit(firstLimit);
            searchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
            List<String> msgIds = TestUtil.searchMessageIds(zmbx, searchParams);
            assertEquals(firstLimit,msgIds.size());
            int gotMessages = msgIds.size();
            List<String> seenIds = new ArrayList<String>();
            seenIds.addAll(msgIds);
            int recCount = 1;
            offset+=firstLimit;
            while(gotMessages > 0) {
                searchParams = new ZSearchParams("in:inbox " + SEARCH_STRING);
                searchParams.setSortBy(SearchSortBy.dateDesc);
                searchParams.setLimit(incLimit);
                searchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
                searchParams.setOffset(offset);
                msgIds = TestUtil.searchMessageIds(zmbx, searchParams);
                recCount++;
                gotMessages = msgIds.size();
                int resCount = 0;
                for(String szId : msgIds) {
                    assertFalse(String.format("Request %d, result %d, encountered duplicate ID %s. Previously seen at %d", recCount, resCount, szId, seenIds.indexOf(szId)), seenIds.contains(szId));
                    seenIds.add(szId);
                    resCount++;
                }
                Thread.sleep(100); //jetty sometimes crashes on Mac when bombarded with request without a timeout
                offset+=incLimit;
            }
            for(int i=0;i<expectedIds.size();i++) {
                assertEquals("IDs at index " + i + " do not match", expectedIds.get(i),seenIds.get(i) );
             }
            assertEquals("Returned incorrect number of messages", numMessages, seenIds.size());
        } catch (ServiceException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    @Override
    public void tearDown()
    throws Exception {
        cleanUp();
    }
    private void cleanUp()
    throws Exception {
        Provisioning prov = Provisioning.getInstance();
        Account account = prov.get(AccountBy.name, TestUtil.getAddress(RECIPIENT));
        if (account != null) {
            prov.deleteAccount(account.getId());
        }

        account = prov.get(AccountBy.name, TestUtil.getAddress(RECIPIENT2));
        if (account != null) {
            prov.deleteAccount(account.getId());
        }
    }

}
