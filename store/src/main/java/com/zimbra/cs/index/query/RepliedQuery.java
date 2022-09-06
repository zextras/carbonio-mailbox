// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

/**
 * Query messages tagged with Answered.
 *
 * @author tim
 * @author ysasaki
 */
public final class RepliedQuery extends TagQuery {

  public RepliedQuery(boolean bool) {
    super("\\Answered", bool);
  }

  @Override
  public void dump(StringBuilder out) {
    super.dump(out);
    out.append(getBool() ? ",REPLIED" : ",UNREPLIED");
  }
}
