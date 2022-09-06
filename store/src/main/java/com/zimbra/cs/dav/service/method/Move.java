// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.resource.Collection;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.resource.MailItemResource;
import com.zimbra.cs.dav.resource.Notebook;
import com.zimbra.cs.dav.service.DavMethod;
import com.zimbra.cs.mailbox.MailItem;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class Move extends DavMethod {
  public static final String MOVE = "MOVE";

  public String getName() {
    return MOVE;
  }

  protected MailItemResource mir = null;
  protected Collection col = null;

  @Override
  public void checkPrecondition(DavContext ctxt) throws DavException, ServiceException {
    super.checkPrecondition(ctxt);
    DavResource rs = ctxt.getRequestedResource();
    if (!(rs instanceof MailItemResource))
      throw new DavException("cannot move", HttpServletResponse.SC_BAD_REQUEST, null);
    col = ctxt.getDestinationCollection();
    mir = (MailItemResource) rs;
    if (!mir.isCollection()) {
      Collection srcCollection = ctxt.getRequestedParentCollection();
      if (srcCollection.getDefaultView() != MailItem.Type.UNKNOWN
          && srcCollection.getDefaultView() != col.getDefaultView())
        throw new DavException(
            "cannot move to incompatible collection", HttpServletResponse.SC_FORBIDDEN, null);
    } else {
      // allow moving of collections of type document or unknown only.
      if (!(((Collection) mir).getDefaultView() == MailItem.Type.DOCUMENT
          || ((Collection) mir).getDefaultView() == MailItem.Type.UNKNOWN))
        throw new DavException(
            "cannot move non-document collection", HttpServletResponse.SC_FORBIDDEN, null);
      // do not allow moving of collection if destination type is not document or unknown.
      if (!(col.getDefaultView() == MailItem.Type.UNKNOWN
          || col.getDefaultView() == MailItem.Type.DOCUMENT))
        throw new DavException(
            "cannot move to incompatible collection", HttpServletResponse.SC_FORBIDDEN, null);
    }
  }

  public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
    String newName = null;
    if (mir instanceof Collection || mir instanceof Notebook) newName = ctxt.getNewName();
    if (ctxt.isOverwriteSet()) {
      mir.moveORcopyWithOverwrite(ctxt, col, newName, true);
    } else {
      mir.move(ctxt, col, newName);
    }
    ctxt.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
