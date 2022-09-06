// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.zimbra.common.service.ServiceException;
import java.io.Closeable;
import java.util.List;

/**
 * Interface for iterating through {@link ZimbraHit}s. This class is the thing that is returned when
 * you do a Search.
 *
 * @since Mar 15, 2005
 * @author tim
 */
public interface ZimbraQueryResults extends Closeable {

  /**
   * Resets the iterator to the beginning
   *
   * @throws ServiceException
   */
  void resetIterator() throws ServiceException;

  /**
   * @return The next hit, advancing the iterator.
   * @throws ServiceException
   */
  ZimbraHit getNext() throws ServiceException;

  /**
   * @return The next hit without advancing the iterator.
   * @throws ServiceException
   */
  ZimbraHit peekNext() throws ServiceException;

  /**
   * Slightly more efficient in a few cases (DB-only queries), skip to a specific hit offset.
   *
   * @param hitNo
   * @return
   * @throws ServiceException
   */
  ZimbraHit skipToHit(int hitNo) throws ServiceException;

  /**
   * @return TRUE if there is another Hit
   * @throws ServiceException
   */
  boolean hasNext() throws ServiceException;

  /**
   * Note that in some cases, this might be a different Sort from the one passed into
   * Mailbox.Search() -- if the sort is overridden by a "Sort:" operator in the search string.
   *
   * @return The Sort used by these results.
   */
  SortBy getSortBy();

  /**
   * {@link QueryInfo} is returned from the Search subsystem with meta information about the search,
   * such as information about wildcard expansion, etc.
   *
   * @return
   */
  List<QueryInfo> getResultInfo();

  /**
   * Returns the cursor offset from the top, or -1 if undetermined.
   *
   * @return offset of the cursor position from the top
   */
  long getCursorOffset();

  /**
   * @return true if results are already sorted in the desired order - for instance they are based
   *     on a proxied search.
   */
  public boolean isPreSorted();
}
