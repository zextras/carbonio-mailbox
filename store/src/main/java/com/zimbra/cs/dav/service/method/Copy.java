// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.resource.Collection;
import com.zimbra.cs.dav.resource.Notebook;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class Copy extends Move {
  public static final String COPY = "COPY";

  public String getName() {
    return COPY;
  }

  public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
    String newName = null;
    if (mir instanceof Collection || mir instanceof Notebook) newName = ctxt.getNewName();
    if (ctxt.isOverwriteSet()) {
      mir.moveORcopyWithOverwrite(ctxt, col, newName, false);
    } else {
      mir.copy(ctxt, col, newName);
    }
    ctxt.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
