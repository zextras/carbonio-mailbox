// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;

/**
 * Abstraction of index write operations.
 *
 * @see IndexStore#openIndexer()
 * @author ysasaki
 * @author smukhopadhyay
 */
public interface Indexer extends Closeable {

    /**
     * Adds index documents.
     */
    void addDocument(Folder folder, MailItem item, List<IndexDocument> docs) throws IOException;

    /**
     * Deletes index documents.
     *
     * @param ids list of item IDs to delete
     */
    void deleteDocument(List<Integer> ids) throws IOException;

    /**
     * Compacts the index by expunging all the deletes.
     */
    void compact();

    /**
     * Modeled on {@link org.apache.lucene.index.IndexReader} {@code maxDoc()} whose description is: <br />
     * Returns total number of docs in this index, including docs not yet flushed (still in the RAM buffer),
     * not counting deletions.  Note that this is a cached value.
     * <p>Used from SOAP GetIndexStatsRequest</p>
     * @return total number of documents in this index excluding deletions
     */
    int maxDocs();
}
