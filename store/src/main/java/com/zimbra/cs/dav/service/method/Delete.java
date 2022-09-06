// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.service.DavMethod;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class Delete extends DavMethod {
  public static final String DELETE = "DELETE";

  public String getName() {
    return DELETE;
  }

  public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
    DavResource rsc = ctxt.getRequestedResource();
    if (rsc == null)
      throw new DavException("cannot find the resource", HttpServletResponse.SC_NOT_FOUND, null);
    rsc.delete(ctxt);
    ctxt.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
