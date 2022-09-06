// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public class SearchCommand extends ImapCommand {
  private ImapSearch search;
  private Integer options;

  public SearchCommand(ImapSearch search, Integer options) {
    super();
    this.search = search;
    this.options = options;
  }

  public ImapSearch getSearch() {
    return search;
  }

  public Integer getOptions() {
    return options;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((options == null) ? 0 : options.hashCode());
    result = prime * result + ((search == null) ? 0 : search.hashCode());
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
    SearchCommand other = (SearchCommand) obj;
    if (options == null) {
      if (other.options != null) {
        return false;
      }
    } else if (!options.equals(other.options)) {
      return false;
    }
    if (search == null) {
      if (other.search != null) {
        return false;
      }
    } else if (!search.equals(other.search)) {
      return false;
    }
    return true;
  }
}
