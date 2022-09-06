// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import java.util.Set;

public interface AliasedEntry {

  /*
   * entry with aliases
   */

  /**
   * returns whether addr is the entry's primary address or one of the aliases.
   *
   * @param addr
   * @return
   */
  public boolean isAddrOfEntry(String addr);

  public String[] getAliases() throws ServiceException;

  /**
   * returns all addresses of the entry, including primary address and all aliases.
   *
   * @return
   */
  public Set<String> getAllAddrsSet();
}
