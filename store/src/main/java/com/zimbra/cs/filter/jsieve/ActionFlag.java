// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.jsieve.mail.Action;

/**
 * @since Nov 8, 2004
 */
public final class ActionFlag implements Action {

  private static final Map<String, ActionFlag> FLAGS =
      ImmutableMap.<String, ActionFlag>builder()
          .put("read", new ActionFlag(com.zimbra.cs.mailbox.Flag.FlagInfo.UNREAD, false, "read"))
          .put("unread", new ActionFlag(com.zimbra.cs.mailbox.Flag.FlagInfo.UNREAD, true, "unread"))
          .put(
              "flagged",
              new ActionFlag(com.zimbra.cs.mailbox.Flag.FlagInfo.FLAGGED, true, "flagged"))
          .put(
              "unflagged",
              new ActionFlag(com.zimbra.cs.mailbox.Flag.FlagInfo.FLAGGED, false, "unflagged"))
          .put(
              "priority",
              new ActionFlag(com.zimbra.cs.mailbox.Flag.FlagInfo.PRIORITY, true, "priority"))
          .put(
              "unpriority",
              new ActionFlag(com.zimbra.cs.mailbox.Flag.FlagInfo.PRIORITY, false, "priority"))
          .build();

  private final com.zimbra.cs.mailbox.Flag.FlagInfo flag;
  private final boolean set;
  private final String name;

  public static ActionFlag of(String name) {
    return FLAGS.get(name);
  }

  private ActionFlag(com.zimbra.cs.mailbox.Flag.FlagInfo flag, boolean set, String name) {
    this.flag = flag;
    this.set = set;
    this.name = name;
  }

  public com.zimbra.cs.mailbox.Flag.FlagInfo getFlag() {
    return flag;
  }

  public boolean isSet() {
    return set;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("set", set).toString();
  }
}
