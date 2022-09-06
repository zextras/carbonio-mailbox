// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.cs.imap.ImapHandler.StoreAction;
import java.util.List;

public class StoreCommand extends ImapCommand {
  private String sequenceSet;
  private List<String> flagNames;
  private StoreAction operation;
  int modseq;

  public StoreCommand(
      String sequenceSet, List<String> flagNames, StoreAction operation, int modseq) {
    super();
    this.sequenceSet = sequenceSet;
    this.flagNames = flagNames;
    this.operation = operation;
    this.modseq = modseq;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((flagNames == null) ? 0 : flagNames.hashCode());
    result = prime * result + modseq;
    result = prime * result + ((operation == null) ? 0 : operation.hashCode());
    result = prime * result + ((sequenceSet == null) ? 0 : sequenceSet.hashCode());
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
    StoreCommand other = (StoreCommand) obj;
    if (flagNames == null) {
      if (other.flagNames != null) {
        return false;
      }
    } else if (!flagNames.equals(other.flagNames)) {
      return false;
    }
    if (modseq != other.modseq) {
      return false;
    }
    if (operation == null) {
      if (other.operation != null) {
        return false;
      }
    } else if (!operation.equals(other.operation)) {
      return false;
    }
    if (sequenceSet == null) {
      if (other.sequenceSet != null) {
        return false;
      }
    } else if (!sequenceSet.equals(other.sequenceSet)) {
      return false;
    }
    return true;
  }
}
