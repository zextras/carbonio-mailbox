// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;

/**
 * A set of UngroupedQueryResults which groups by Conversation.
 *
 * @author tim
 */
final class ConvQueryResults extends ZimbraQueryResultsImpl {

    private final ZimbraQueryResults results;
    private ZimbraHit nextHit;
    private int nextHitNo = 0;
    private List<ZimbraHit> cachedResults = new ArrayList<ZimbraHit>();
    private Set<Integer> seenConvIDs = new HashSet<Integer>();

    ConvQueryResults(ZimbraQueryResults results, Set<MailItem.Type> types, SortBy sort, SearchParams.Fetch fetch) {
        super(types, sort, fetch);
        this.results = results;
    }

    @Override
    public long getCursorOffset() {
        return results.getCursorOffset();
    }

    /**
     * Get the next Conversation Hit from our subOp
     *
     * Side-Effect: always advances the SubOp pointer!
     */
    private ZimbraHit internalGetNextHit() throws ServiceException {
        while (results.hasNext()) {
            ZimbraHit opNext = results.getNext();

            if (!isConversation(opNext)) {
                return opNext;
            }

            int convId = opNext.getConversationId();
            if (seenConvIDs.contains(convId)) {
                // we've seen this conv before (and therefore reported it),
                // so just do nothing right now...
            } else {
                ConversationHit curHit = null;
                if (opNext instanceof ConversationHit) {
                    curHit = (ConversationHit) opNext;
                } else if (opNext instanceof MessageHit) {
                    curHit = ((MessageHit) opNext).getConversationResult();
                } else if (opNext instanceof MessagePartHit) {
                    curHit = ((MessagePartHit) opNext).getMessageResult().getConversationResult();
                } else {
                    assert(false);
                }
                seenConvIDs.add(convId);

                // iterate further: try to get all the hits for this result
                // Conversation if they happen to be right here with the first one
                while (results.hasNext()) {
                    ZimbraHit nextHit = results.peekNext();
                    if (!isConversation(nextHit)) {
                        return curHit;
                    } else {
                        if (nextHit.getConversationId() != convId) {
                            return curHit; // no more, all done!
                        } else {
                            // same conv.  Consume this hit!
                            results.getNext();

                            //  Now add this hit to the conv we're gonna return
                            if (nextHit instanceof MessageHit) {
                                curHit.addMessageHit((MessageHit) nextHit);
                            } else if (nextHit instanceof MessagePartHit) {
                                curHit.addMessageHit(((MessagePartHit) nextHit).getMessageResult());
                            }
                        }
                    }
                }
                return curHit;
            }
        }
        return null;
    }

    private boolean isConversation(ZimbraHit hit) {
        return (hit instanceof MessageHit || hit instanceof MessagePartHit || hit instanceof ConversationHit);
    }

    private boolean bufferNextHit() throws ServiceException {
        if (nextHit == null) {
            nextHit = internalGetNextHit();
        }
        return (nextHit != null);
    }

    @Override
    public ZimbraHit peekNext() throws ServiceException {
        bufferNextHit();
        return nextHit;
    }

    @Override
    public void resetIterator() throws ServiceException {
        seenConvIDs.clear();
        results.resetIterator();
        nextHitNo = 0;
        cachedResults.clear(); // must clear since we clear mSeenConvIDs
        nextHit = null;
    }

    @Override
    public ZimbraHit getNext() throws ServiceException {
        ZimbraHit hit = null;
        if (cachedResults.size() > nextHitNo) {
            hit = cachedResults.get(nextHitNo);
        } else if (bufferNextHit()) {
            hit = nextHit;
            cachedResults.add(nextHitNo, nextHit);
            nextHit = null;
        }
        if (hit != null) {
            nextHitNo++;
        }
        return hit;
    }

    @Override
    public void close() throws IOException {
        results.close();
    }

    @Override
    public ZimbraHit skipToHit(int hitNo) throws ServiceException {
        if (hitNo == 0) {
            resetIterator();
            return getNext();
        } else {
            ZimbraHit hit = null;
          nextHitNo = Math.min(hitNo, cachedResults.size());
            while (nextHitNo <= hitNo) {
                hit = getNext();
                if (hit == null) {
                    break;
                }
            }
            return hit;
        }
    }

    @Override
    public List<QueryInfo> getResultInfo() {
        return results.getResultInfo();
    }

}

