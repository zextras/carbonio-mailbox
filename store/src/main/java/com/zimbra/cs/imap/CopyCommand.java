// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public class CopyCommand extends ImapCommand {

  private ImapPath destPath;
  private String sequenceSet;

  public CopyCommand(String sequenceSet, ImapPath destPath) {
    super();
    this.destPath = destPath;
    this.sequenceSet = sequenceSet;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sequenceSet == null) ? 0 : sequenceSet.hashCode());
    result = prime * result + ((destPath == null) ? 0 : destPath.hashCode());
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
    CopyCommand other = (CopyCommand) obj;
    if (sequenceSet == null) {
      if (other.sequenceSet != null) {
        return false;
      }
    } else if (!sequenceSet.equals(other.sequenceSet)) {
      return false;
    }
    if (destPath == null) {
      if (other.destPath != null) {
        return false;
      }
    } else if (!destPath.equals(other.destPath)) {
      return false;
    }
    return true;
  }
}
