// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Conversation;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailItem;

/**
 * Indirect {@link Conversation} result. Efficient Read-access to a {@link Conversation} object returned from a query.
 * <p>
 * This class may have a real {@link Conversation} under it, or it might just have a Lucene Document, or something else
 * -- the accessor APIs in this class will do the most efficient possible lookup and caching for read access to the data.
 *
 * @since Oct 15, 2004
 * @author tim
 */
public final class ConversationHit extends ZimbraHit {

    private final int conversationId;
    private Conversation conversation;
    private Map<Long, MessageHit> messageHits = new LinkedHashMap<>();

    ConversationHit(ZimbraQueryResultsImpl results, Mailbox mbx, int convId, Object sortKey) {
        super(results, mbx, sortKey);
        conversationId = convId;
    }

    @Override
    public int getConversationId() {
        return getId();
    }

    public void addMessageHit(MessageHit hit) {
        messageHits.put((long) hit.getItemId(), hit);
    }

    public Collection<MessageHit> getMessageHits() {
        return messageHits.values();
    }

    public int getNumMessageHits() {
        return getMessageHits().size();
    }

    public int getNumMessages() throws ServiceException {
        return getConversation().getMessageCount();
    }

    public MessageHit getMessageHit(Long mailboxBlobId) {
        return messageHits.get(mailboxBlobId);
    }

    public MessageHit getFirstMessageHit() {
        Iterator<MessageHit> iter = getMessageHits().iterator();
        return iter.hasNext() ? iter.next() : null;
    }

    @Override
    public int getItemId() {
        return conversationId;
    }

    @Override
    void setItem(MailItem item) {
        conversation = (Conversation) item;
    }

    @Override
    boolean itemIsLoaded() {
        return conversation != null;
    }

    public int getId() {
        return conversationId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("item", getId()).addValue(super.toString()).toString();
    }

    @Override
    public String getName() throws ServiceException {
        if (cachedName == null) {
            MessageHit mh = getFirstMessageHit();
            cachedName = mh == null ? "" : mh.getName();
        }
        return cachedName;
    }

    @Override
    public MailItem getMailItem() throws ServiceException {
        return getConversation();
    }

    /**
     * Returns the real {@link Conversation} object. Only use this if you need write access to the Conversation.
     *
     * Depending on the type of query that was executed, this may or may not result in a DB access
     *
     * @return real Conversation object
     */
    public Conversation getConversation() throws ServiceException {
        if (conversation == null) {
            conversation = getMailbox().getConversationById(null, conversationId);
        }
        return conversation;
    }
}
