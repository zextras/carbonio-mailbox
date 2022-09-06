// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import com.zimbra.cs.index.LuceneQueryOperation;
import com.zimbra.cs.index.QueryOperation;
import com.zimbra.cs.mailbox.Mailbox;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

/**
 * Query by email domain.
 *
 * @author tim
 * @author ysasaki
 */
public final class DomainQuery extends Query {
  private final String field;
  private final String term;

  public DomainQuery(String field, String term) {
    this.field = field;
    this.term = term;
  }

  @Override
  public boolean hasTextOperation() {
    return true;
  }

  @Override
  public QueryOperation compile(Mailbox mbox, boolean bool) {
    LuceneQueryOperation op = new LuceneQueryOperation();
    op.addClause(toQueryString(field, term), new TermQuery(new Term(field, term)), evalBool(bool));
    return op;
  }

  @Override
  public void dump(StringBuilder out) {
    out.append("DOMAIN:");
    out.append(term);
  }

  @Override
  public void sanitizedDump(StringBuilder out) {
    out.append("DOMAIN:");
    out.append("$TEXT");
  }
}
