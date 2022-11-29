// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import org.apache.lucene.analysis.Analyzer;

import com.zimbra.cs.index.DBQueryOperation;
import com.zimbra.cs.index.LuceneFields;
import com.zimbra.cs.index.QueryOperation;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * Query by subject.
 *
 * @author tim
 * @author ysasaki
 */
public final class SubjectQuery extends Query {
    private String subject;
    private boolean lt;
    private boolean inclusive;

    /**
     * This is only used for subject queries that start with {@code <} or {@code >}, otherwise we just use the normal
     * {@link TextQuery}.
     */
    private SubjectQuery(String text) {
        lt = (text.charAt(0) == '<');
        inclusive = false;
        subject = text.substring(1);

        if (subject.charAt(0) == '=') {
            inclusive = true;
            subject = subject.substring(1);
        }
    }

    public static Query create(Analyzer analyzer, String text) {
        if (text.length() > 1 && (text.startsWith("<") || text.startsWith(">"))) {
            // real subject query!
            return new SubjectQuery(text);
        } else {
            return new TextQuery(analyzer, LuceneFields.L_H_SUBJECT, text);
        }
    }

    @Override
    public boolean hasTextOperation() {
        return false;
    }

    @Override
    public QueryOperation compile(Mailbox mbox, boolean bool) {
        DBQueryOperation op = new DBQueryOperation();
        if (lt) {
            op.addSubjectRange(null, false, subject, inclusive, evalBool(bool));
        } else {
            op.addSubjectRange(subject, inclusive, null, false, evalBool(bool));
        }
        return op;
    }

    @Override
    public void dump(StringBuilder out) {
        out.append("SUBJECT:");
        out.append(lt ? '<' : '>');
        if (inclusive) {
            out.append('=');
        }
        out.append(subject);
    }

    @Override
    public void sanitizedDump(StringBuilder out) {
        out.append("SUBJECT:");
        out.append(lt ? '<' : '>');
        if (inclusive) {
            out.append('=');
        }
        out.append("$TEXT");
    }
}
