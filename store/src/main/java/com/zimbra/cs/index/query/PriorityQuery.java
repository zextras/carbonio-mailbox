// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import com.google.common.base.Preconditions;

/**
 * Query messages by priority.
 *
 * <p>
 *
 * <ul>
 *   <li>High priority messages are tagged with {@code \Urgent}.
 *   <li>Low priority messages are tagged with {@code \Bulk}.
 * </ul>
 *
 * @author ysasaki
 */
public final class PriorityQuery extends TagQuery {

  public enum Priority {
    HIGH("\\Urgent"),
    LOW("\\Bulk");

    private final String flag;

    private Priority(String flag) {
      this.flag = flag;
    }

    private String toFlag() {
      return flag;
    }
  }

  private final Priority priority;

  public PriorityQuery(Priority priority) {
    super(Preconditions.checkNotNull(priority).toFlag(), true);
    this.priority = priority;
  }

  @Override
  public void dump(StringBuilder out) {
    out.append("PRIORITY:");
    out.append(priority.name());
  }
}
