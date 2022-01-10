// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.List;

import org.apache.lucene.search.TopDocs;

import com.google.common.base.MoreObjects;

/**
 * Used for results of Zimbra Index searches.
 */
public class ZimbraTopDocs {
    private final int totalHits;
    private final float maxScore;
    private final List<ZimbraScoreDoc> scoreDocs;

    protected ZimbraTopDocs(int totalHits, List<ZimbraScoreDoc> scoreDocs, float maxScore) {
        this.totalHits = totalHits;
        this.scoreDocs = scoreDocs;
        this.maxScore = maxScore;
    }

    public static ZimbraTopDocs create(int totalHits, List<ZimbraScoreDoc> scoreDocs, float maxScore) {
        return new ZimbraTopDocs(totalHits, scoreDocs, maxScore);
    }

    /**
     * Create a ZimbraTopDocs object for search results where scores are not tracked.
     */
    public static ZimbraTopDocs create(int totalHits, List<ZimbraScoreDoc> scoreDocs) {
        return new ZimbraTopDocs(totalHits, scoreDocs, Float.NaN);
    }

    /**
     * Create equivalent ZimbraTopDocs object to a Lucene TopDocs object
     */
    public static ZimbraTopDocs create(TopDocs luceneTopDocs) {
        return new ZimbraTopDocs(luceneTopDocs.totalHits,
                ZimbraScoreDoc.listFromLuceneScoreDocs(luceneTopDocs.scoreDocs), luceneTopDocs.getMaxScore());
    }

    /**
     * Returns maximum score encountered or {@link Float#NaN} if scores are not tracked.
     */
    public float getMaxScore() {
        return maxScore;
    }

    /**
     * Returns total number of hits for the query.
     */
    public int getTotalHits() {
        return totalHits;
    }

    /**
     * Returns top hits for the query.
     */
    public List<ZimbraScoreDoc> getScoreDocs() {
        return scoreDocs;
    }

    /**
     * Returns ScoreDoc at index {@code index}
     */
    public ZimbraScoreDoc getScoreDoc(int index) {
        return scoreDocs.get(index);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("totalHits", totalHits)
                .add("maxScore", maxScore)
                .add("scoreDocs", scoreDocs).toString();
    }
}
