// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.util.List;

import com.zimbra.cs.index.QueryOperation;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * Special query that wraps sub queries.
 *
 * @author tim
 * @author ysasaki
 */
public class SubQuery extends Query {

    private final List<Query> clauses;

    public SubQuery(List<Query> clauses) {
        this.clauses = clauses;
    }

    public List<Query> getSubClauses() {
        return clauses;
    }

    @Override
    public boolean hasTextOperation() {
        for (Query sub : clauses) {
            if (sub.hasTextOperation()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public QueryOperation compile(Mailbox mbox, boolean bool) {
        assert false;
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder toString(StringBuilder out) {
        out.append(getModifier());
        out.append('(');
        dump(out);
        return out.append(')');
    }

    @Override
    public void dump(StringBuilder out) {
        for (Query sub : clauses) {
            sub.toString(out);
        }
    }

    @Override
    public void sanitizedDump(StringBuilder out) {
        for (Query sub : clauses) {
            sub.toSanitizedString(out);
        }
    }

}
