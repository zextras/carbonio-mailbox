// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.List;

import org.apache.lucene.search.ScoreDoc;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * Used for results of Zimbra Index searches.
 */
public final class ZimbraScoreDoc {
    private final ZimbraIndexDocumentID documentID;
    private final float score;

    private ZimbraScoreDoc(ZimbraIndexDocumentID documentID, float score) {
        this.documentID = documentID;
        this.score = score;
    }



    /**
     * Create equivalent ZimbraScoreDoc object to a Lucene ScoreDoc object
     */
    public static ZimbraScoreDoc create(ScoreDoc luceneScoreDoc) {
        return new ZimbraScoreDoc(new ZimbraLuceneDocumentID(luceneScoreDoc.doc), luceneScoreDoc.score);
    }

    /**
     * Returns the index store's ID for this document
     */
    public ZimbraIndexDocumentID getDocumentID() {
        return documentID;
    }


    public static List<ZimbraScoreDoc> listFromLuceneScoreDocs(ScoreDoc[] luceneScoreDocs) {
        if (luceneScoreDocs == null) {
            return Lists.newArrayListWithCapacity(0);
        }
        List<ZimbraScoreDoc> docs = Lists.newArrayListWithCapacity(luceneScoreDocs.length);
        for (ScoreDoc luceneDoc : luceneScoreDocs) {
            docs.add(ZimbraScoreDoc.create(luceneDoc));
        }
        return docs;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("documentID", documentID).add("score", score).toString();
    }
}
