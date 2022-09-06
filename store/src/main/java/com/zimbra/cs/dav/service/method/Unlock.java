// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.dav.LockMgr;
import com.zimbra.cs.dav.service.DavMethod;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class Unlock extends DavMethod {
  public static final String UNLOCK = "UNLOCK";

  public String getName() {
    return UNLOCK;
  }

  public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
    String token = ctxt.getRequest().getHeader(DavProtocol.HEADER_LOCK_TOKEN);
    if (token != null) {
      LockMgr.getInstance()
          .deleteLock(ctxt, ctxt.getUri(), LockMgr.Lock.parseLockTokenHeader(token));
    }
    ctxt.getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
    ctxt.responseSent();
  }
}
