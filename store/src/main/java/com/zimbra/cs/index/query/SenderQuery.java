// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import com.zimbra.cs.index.DBQueryOperation;
import com.zimbra.cs.index.LuceneFields;
import com.zimbra.cs.index.QueryOperation;
import com.zimbra.cs.mailbox.Mailbox;
import org.apache.lucene.analysis.Analyzer;

/**
 * Query by sender.
 *
 * @author tim
 * @author ysasaki
 */
public final class SenderQuery extends Query {
  private final String sender;
  private final Comparison comparison;

  /**
   * This is only used for subject queries that start with {@code <} or {@code >}, otherwise we just
   * use the normal {@link TextQuery}.
   */
  private SenderQuery(String text) {
    if (text.startsWith(Comparison.LE.toString())) {
      comparison = Comparison.LE;
    } else if (text.startsWith(Comparison.LT.toString())) {
      comparison = Comparison.LT;
    } else if (text.startsWith(Comparison.GE.toString())) {
      comparison = Comparison.GE;
    } else if (text.startsWith(Comparison.GT.toString())) {
      comparison = Comparison.GT;
    } else {
      throw new IllegalArgumentException(text);
    }
    sender = text.substring(comparison.toString().length());
  }

  public static Query create(Analyzer analyzer, String text) {
    if (text.length() > 1
        && (text.startsWith(Comparison.LT.toString())
            || text.startsWith(Comparison.GT.toString()))) {
      return new SenderQuery(text);
    } else {
      return new TextQuery(analyzer, LuceneFields.L_H_FROM, text);
    }
  }

  @Override
  public boolean hasTextOperation() {
    return false;
  }

  @Override
  public QueryOperation compile(Mailbox mbox, boolean bool) {
    DBQueryOperation op = new DBQueryOperation();
    switch (comparison) {
      case LE:
        op.addSenderRange(null, false, sender, true, evalBool(bool));
        break;
      case LT:
        op.addSenderRange(null, false, sender, false, evalBool(bool));
        break;
      case GE:
        op.addSenderRange(sender, true, null, false, evalBool(bool));
        break;
      case GT:
        op.addSenderRange(sender, false, null, false, evalBool(bool));
        break;
      default:
        assert false : comparison;
    }
    return op;
  }

  @Override
  public void dump(StringBuilder out) {
    out.append("SENDER:").append(comparison).append(sender);
  }

  @Override
  public void sanitizedDump(StringBuilder out) {
    out.append("SENDER:").append(comparison).append("$TEXT");
  }
}
