// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.index.SearchParams;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.index.ZimbraQuery;
import com.zimbra.cs.index.ZimbraQueryResults;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.Search;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.SearchRequest;
import com.zimbra.soap.mail.message.SearchResponse;
import com.zimbra.soap.type.SearchHit;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link TextQuery}.
 *
 * @author ysasaki
 */
public final class TextQueryTest extends MailboxTestSuite {

    private Account account;
    private Mailbox mailbox;

    @BeforeEach
    public void init() throws Exception {
        account = createAccount().create();
        mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
    }

    @Test
    void toQueryStringSingleTerm() throws Exception {
        Mailbox mbox = mailbox;
        MailboxTestUtil.index(mbox);

        SearchParams params = new SearchParams();
        params.setQueryString("content:one");
        params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
        params.setSortBy(SortBy.NONE);

        ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox, params);

        assertEquals("(content:one)", query.toQueryString());
    }

    @Test
    void toQueryStringMultiTerms() throws Exception {
        Mailbox mbox = mailbox;
        MailboxTestUtil.index(mbox);

        SearchParams params = new SearchParams();
        params.setQueryString("content:\"one two three\"");
        params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
        params.setSortBy(SortBy.NONE);

        ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox, params);

        assertEquals("(content:\"one two three\")", query.toQueryString());
    }

    @Test
    void wildcardExpandedToNone() throws Exception {
        Mailbox mbox = mailbox;

        ZimbraQueryResults results = mbox.index.search(new OperationContext(mbox), "none*",
                EnumSet.of(MailItem.Type.MESSAGE), SortBy.NONE, 100);
        assertFalse(results.hasNext());

        results = mbox.index.search(new OperationContext(mbox), "from:none* AND subject:none*",
                EnumSet.of(MailItem.Type.MESSAGE), SortBy.NONE, 100);
        assertFalse(results.hasNext());

        results = mbox.index.search(new OperationContext(mbox), "from:none* OR subject:none*",
                EnumSet.of(MailItem.Type.MESSAGE), SortBy.NONE, 100);
        assertFalse(results.hasNext());
    }

    @Test
    void sortByHasAttach() throws Exception {
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        // setup: add a message
        DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
        Message msg = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
        Message msgWithAttach = mbox.addMessage(null,
                MailboxTestUtil.generateMessageWithAttachment("test subject has attach"), dopt, null);

        SearchResponse resp;
        List<SearchHit> hits;
        int msgId;
        int msgWithAttachId;
        SearchRequest sr = new SearchRequest();
        sr.setSearchTypes("message");
        sr.setQuery("test");
        sr.setSortBy(SortBy.ATTACHMENT_ASC.toString());
        resp = doSearch(sr, account);
        hits = resp.getSearchHits();
        assertEquals(2, hits.size(), "Number of hits");
        msgId = Integer.parseInt(hits.get(0).getId());
        msgWithAttachId = Integer.parseInt(hits.get(1).getId());
        assertEquals(msg.getId(), msgId, "correct hit ascending no attachments");
        assertEquals(msgWithAttach.getId(), msgWithAttachId, "correct hit ascending has attachments");

        /* Check that we get them in the opposite order if we change the search direction */
        sr.setSortBy(SortBy.ATTACHMENT_DESC.toString());
        resp = doSearch(sr, account);
        hits = resp.getSearchHits();
        assertEquals(2, hits.size(), "Number of hits");
        msgId = Integer.parseInt(hits.get(1).getId());
        msgWithAttachId = Integer.parseInt(hits.get(0).getId());
        assertEquals(msg.getId(), msgId, "correct hit descending no attachments");
        assertEquals(msgWithAttach.getId(), msgWithAttachId, "correct hit descending has attachments");
    }

    @Test
    void sortByIsFlagged() throws Exception {
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        // setup: add a message
        DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
        Message msg = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
        dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(
                Flag.BITMASK_UNREAD | Flag.BITMASK_FLAGGED);
        Message msgWithFlag = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject flag"), dopt, null);

        SearchResponse resp;
        List<SearchHit> hits;
        int msgId;
        int msgWithFlagId;
        SearchRequest sr = new SearchRequest();
        sr.setSearchTypes("message");
        sr.setQuery("test");
        sr.setSortBy(SortBy.FLAG_ASC.toString());
        resp = doSearch(sr, account);
        hits = resp.getSearchHits();
        assertEquals(2, hits.size(), "Number of hits");
        msgId = Integer.parseInt(hits.get(0).getId());
        msgWithFlagId = Integer.parseInt(hits.get(1).getId());
        assertEquals(msg.getId(), msgId, "correct hit ascending unflagged");
        assertEquals(msgWithFlag.getId(), msgWithFlagId, "correct hit ascending flagged");

        /* Check that we get them in the opposite order if we change the search direction */
        sr.setSortBy(SortBy.FLAG_DESC.toString());
        resp = doSearch(sr, account);
        hits = resp.getSearchHits();
        assertEquals(2, hits.size(), "Number of hits");
        msgId = Integer.parseInt(hits.get(1).getId());
        msgWithFlagId = Integer.parseInt(hits.get(0).getId());
        assertEquals(msg.getId(), msgId, "correct hit descending unflagged");
        assertEquals(msgWithFlag.getId(), msgWithFlagId, "correct hit descending flagged");
    }

    @Test
    void sortByPriority() throws Exception {
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        // setup: add a message
        DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
        Message msg = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
        Message msgWithHighPri = mbox.addMessage(null,
                MailboxTestUtil.generateHighPriorityMessage("test subject is HI-PRI"), dopt, null);
        Message msgWithLowPri = mbox.addMessage(null,
                MailboxTestUtil.generateLowPriorityMessage("test subject is LOW-PRI"), dopt, null);

        SearchResponse resp;
        List<SearchHit> hits;
        int msgId;
        int msgWithLowPriId;
        int msgWithHiPriId;
        SearchRequest sr = new SearchRequest();
        sr.setSearchTypes("message");
        sr.setQuery("test");
        sr.setSortBy(SortBy.PRIORITY_ASC.toString());
        resp = doSearch(sr, account);
        hits = resp.getSearchHits();
        assertEquals(3, hits.size(), "Number of hits");
        msgId = Integer.parseInt(hits.get(1).getId());
        msgWithHiPriId = Integer.parseInt(hits.get(2).getId());
        msgWithLowPriId = Integer.parseInt(hits.get(0).getId());
        assertEquals(msgWithHighPri.getId(), msgWithHiPriId, "correct hit ascending high");
        assertEquals(msg.getId(), msgId, "correct hit ascending med");
        assertEquals(msgWithLowPri.getId(), msgWithLowPriId, "correct hit ascending low");

        /* Check that we get them in the opposite order if we change the search direction */
        sr.setSortBy(SortBy.PRIORITY_DESC.toString());
        resp = doSearch(sr, account);
        hits = resp.getSearchHits();
        assertEquals(3, hits.size(), "Number of hits");
        msgId = Integer.parseInt(hits.get(1).getId());
        msgWithHiPriId = Integer.parseInt(hits.get(0).getId());
        msgWithLowPriId = Integer.parseInt(hits.get(2).getId());
        assertEquals(msgWithHighPri.getId(), msgWithHiPriId, "correct hit descending high");
        assertEquals(msg.getId(), msgId, "correct hit descending med");
        assertEquals(msgWithLowPri.getId(), msgWithLowPriId, "correct hit descending low");
    }

    static SearchResponse doSearch(SearchRequest request, Account acct) throws Exception {
        Element response = new Search().handle(JaxbUtil.jaxbToElement(request, Element.XMLElement.mFactory),
                ServiceTestUtil.getRequestContext(acct));
        SearchResponse resp = JaxbUtil.elementToJaxb(response, SearchResponse.class);
        return resp;
    }
}
