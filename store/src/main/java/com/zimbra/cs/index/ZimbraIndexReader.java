// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.Closeable;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Modeled on a subset of {@link org.apache.lucene.index.IndexReader}
 */
public interface ZimbraIndexReader extends Closeable, Cloneable {
    /**
     * Returns the number of documents in this index.
     */
    int numDocs();

    /**
     * Number of documents marked for deletion but not yet fully removed from the index
     * @return number of deleted documents for this index
     */
    int numDeletedDocs();

    /**
     * Returns an enumeration of the String representations for values of terms with {@code field} 
     * positioned to start at the first term with a value greater than {@code firstTermValue}.
     * The enumeration is ordered by String.compareTo().
     */
    TermFieldEnumeration getTermsForField(String field, String firstTermValue) throws IOException;

    interface TermFieldEnumeration extends Enumeration<BrowseTerm>, Closeable {
    }
}
