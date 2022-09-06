// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.mailbox.ZimbraQueryHit;
import com.zimbra.common.mailbox.ZimbraQueryHitResults;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.index.ZimbraQueryResults;
import java.io.IOException;

public class LocalQueryHitResults implements ZimbraQueryHitResults {

  private final ZimbraQueryResults zQueryResults;

  public LocalQueryHitResults(ZimbraQueryResults zqr) {
    zQueryResults = zqr;
  }

  @Override
  public ZimbraQueryHit getNext() throws ServiceException {
    return zQueryResults.getNext();
  }

  @Override
  public void close() throws IOException {
    zQueryResults.close();
  }
}
