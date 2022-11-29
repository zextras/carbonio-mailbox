// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;

import com.zimbra.cs.index.LuceneFields;

/**
 * A simpler way of expressing (to:FOO or from:FOO or cc:FOO).
 *
 * @author tim
 * @author ysasaki
 */
public final class AddrQuery extends SubQuery {

    public enum Address {
        FROM, TO, CC
    }

    private AddrQuery(List<Query> clauses) {
        super(clauses);
    }

    @Override
    public boolean hasTextOperation() {
        return true;
    }

    public static AddrQuery create(Analyzer analyzer, Set<Address> addrs, String text) {
        List<Query> clauses = new ArrayList<Query>();

        if (addrs.contains(Address.FROM)) {
            clauses.add(new TextQuery(analyzer, LuceneFields.L_H_FROM, text));
        }

        if (addrs.contains(Address.TO)) {
            if (!clauses.isEmpty()) {
                clauses.add(new ConjQuery(ConjQuery.Conjunction.OR));
            }
            clauses.add(new TextQuery(analyzer, LuceneFields.L_H_TO, text));
        }

        if (addrs.contains(Address.CC)) {
            if (!clauses.isEmpty()) {
                clauses.add(new ConjQuery(ConjQuery.Conjunction.OR));
            }
            clauses.add(new TextQuery(analyzer, LuceneFields.L_H_CC, text));
        }

        return new AddrQuery(clauses);
    }
}
