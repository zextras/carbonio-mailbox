// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.common.localconfig.LC;

public class CreateCommand extends ImapCommand {

  private ImapPath path;
  private int repeats = 0;

  public CreateCommand(ImapPath path) {
    super();
    this.path = path;
  }

  @Override
  protected boolean throttle(ImapCommand previousCommand) {
    // count number of CREATE commands irrespective of parameters
    // this prevents client from continuously creating new folders
    if (previousCommand instanceof CreateCommand) {
      repeats = ((CreateCommand) previousCommand).repeats + 1;
      return repeats > LC.imap_throttle_command_limit.intValue();
    } else {
      repeats++;
      return false;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CreateCommand other = (CreateCommand) obj;
    if (path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!path.equals(other.path)) {
      return false;
    }
    return true;
  }
}
