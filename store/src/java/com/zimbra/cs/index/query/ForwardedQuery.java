// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

/**
 * Query messages tagged with Forwarded.
 *
 * @author tim
 * @author ysasaki
 */
public final class ForwardedQuery extends TagQuery {

    public ForwardedQuery(boolean bool) {
        super("\\Forwarded", bool);
    }

    @Override
    public void dump(StringBuilder out) {
        super.dump(out);
        out.append(getBool() ? ",FORWARDED" : ",UNFORWARDED");
    }
}
