// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;

/** Used for results of Zimbra Index searches. */
public class ZimbraTopFieldDocs extends ZimbraTopDocs {
  private final List<SortField> sortFields;

  /**
   * @param totalHits Total number of hits for the query.
   * @param scoreDocs The top hits for the query.
   * @param fields The sort criteria used to find the top hits.
   * @param maxScore The maximum score encountered.
   */
  protected ZimbraTopFieldDocs(
      int totalHits, List<ZimbraScoreDoc> scoreDocs, float maxScore, List<SortField> sortFields) {
    super(totalHits, scoreDocs, maxScore);
    this.sortFields = sortFields;
  }

  public static ZimbraTopFieldDocs create(
      int totalHits, List<ZimbraScoreDoc> scoreDocs, float maxScore, List<SortField> sortFields) {
    return new ZimbraTopFieldDocs(totalHits, scoreDocs, maxScore, sortFields);
  }

  /** Create a ZimbraTopFielDocs object for search results where scores are not tracked. */
  public static ZimbraTopFieldDocs create(
      int totalHits, List<ZimbraScoreDoc> scoreDocs, List<SortField> sortFields) {
    return new ZimbraTopFieldDocs(totalHits, scoreDocs, Float.NaN, sortFields);
  }

  /** Create equivalent ZimbraTopFieldDocs object to a Lucene TopFieldDocs object */
  public static ZimbraTopFieldDocs create(TopFieldDocs luceneTopFieldDocs) {
    return new ZimbraTopFieldDocs(
        luceneTopFieldDocs.totalHits,
        ZimbraScoreDoc.listFromLuceneScoreDocs(luceneTopFieldDocs.scoreDocs),
        luceneTopFieldDocs.getMaxScore(),
        Arrays.asList(luceneTopFieldDocs.fields));
  }

  /** Returns the sort criteria used to find the top hits. */
  public List<SortField> getSortFields() {
    return sortFields;
  }

  /** Returns SortField at index {@code index} */
  public SortField getSortField(int index) {
    return sortFields.get(index);
  }

  @Override
  public String toString() {
    return super.toString()
        + MoreObjects.toStringHelper(this).add("sortFields", sortFields).toString();
  }
}
