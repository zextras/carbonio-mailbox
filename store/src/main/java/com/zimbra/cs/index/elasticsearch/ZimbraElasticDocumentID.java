// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.elasticsearch;

import com.google.common.base.MoreObjects;
import com.zimbra.cs.index.ZimbraIndexDocumentID;

public final class ZimbraElasticDocumentID
    implements Comparable<ZimbraElasticDocumentID>, ZimbraIndexDocumentID {

  private final String docID;

  public ZimbraElasticDocumentID(String docID) {
    this.docID = docID;
  }

  public String getDocID() {
    return docID;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("docID", docID).toString();
  }

  @Override
  public int compareTo(ZimbraElasticDocumentID o) {
    return docID.compareTo(o.getDocID());
  }
}
