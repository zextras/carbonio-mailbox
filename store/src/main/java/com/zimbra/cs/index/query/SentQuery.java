// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

/**
 * Query messages tagged with Sent.
 *
 * @author tim
 * @author ysasaki
 */
public final class SentQuery extends TagQuery {

    public SentQuery(boolean bool) {
        super("\\Sent", bool);
    }

    @Override
    public void dump(StringBuilder out) {
        super.dump(out);
        out.append(getBool() ? ",SENT" : ",RECEIVED");
    }
}
