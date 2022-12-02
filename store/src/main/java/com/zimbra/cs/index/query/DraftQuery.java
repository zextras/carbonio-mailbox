// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

/**
 * Query messages tagged with Draft.
 *
 * @author tim
 * @author ysasaki
 */
public final class DraftQuery extends TagQuery {

    public DraftQuery(boolean bool) {
        super("\\Draft", bool);
    }

    @Override
    public void dump(StringBuilder out) {
        super.dump(out);
        out.append(getBool() ? ",DRAFT" : ",UNDRAFT");
    }
}
