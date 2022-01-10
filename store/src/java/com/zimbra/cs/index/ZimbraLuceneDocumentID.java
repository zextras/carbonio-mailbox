// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.base.MoreObjects;

public final class ZimbraLuceneDocumentID implements ZimbraIndexDocumentID {

    private final int luceneDocID;

    public ZimbraLuceneDocumentID(int luceneDocID) {
        this.luceneDocID = luceneDocID;
    }

    public int getLuceneDocID() {
        return luceneDocID;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("docID", luceneDocID).toString();
    }
}
