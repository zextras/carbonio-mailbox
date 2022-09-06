// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * UngroupedQueryResults which do NOT group (ie return parts or messages in whatever mix)
 *
 * @since Nov 3, 2004
 */
final class UngroupedQueryResults extends ZimbraQueryResultsImpl {
  private final ZimbraQueryResults results;

  UngroupedQueryResults(
      ZimbraQueryResults results, Set<MailItem.Type> types, SortBy sort, SearchParams.Fetch fetch) {
    super(types, sort, fetch);
    this.results = results;
  }

  @Override
  public long getCursorOffset() {
    return results.getCursorOffset();
  }

  @Override
  public void resetIterator() throws ServiceException {
    results.resetIterator();
  }

  @Override
  public ZimbraHit getNext() throws ServiceException {
    return results.getNext();
  }

  @Override
  public ZimbraHit peekNext() throws ServiceException {
    return results.peekNext();
  }

  @Override
  public void close() throws IOException {
    results.close();
  }

  @Override
  public ZimbraHit skipToHit(int hitNo) throws ServiceException {
    return results.skipToHit(hitNo);
  }

  @Override
  public List<QueryInfo> getResultInfo() {
    return results.getResultInfo();
  }

  @Override
  public boolean isPreSorted() {
    return results.isPreSorted();
  }
}
