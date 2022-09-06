// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.resource;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.mailbox.Document;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents Notebook / Wiki item.
 *
 * @author jylee
 */
public class Notebook extends MailItemResource {

  private Document mDoc;

  public Notebook(DavContext ctxt, Document doc) throws ServiceException {
    super(ctxt, doc);
    mDoc = doc;
    setCreationDate(doc.getDate());
    setLastModifiedDate(doc.getChangeDate());
    setProperty(DavElements.P_DISPLAYNAME, doc.getName());
    // content length is just an estimate.  the actual content will be larger
    // after chrome composition.
    setProperty(DavElements.P_GETCONTENTLENGTH, Long.toString(doc.getSize()));
    setProperty(DavElements.P_GETCONTENTTYPE, doc.getContentType());
  }

  @Override
  public InputStream getContent(DavContext ctxt) throws IOException, DavException {
    try {
      return mDoc.getContentStream();
    } catch (ServiceException se) {
      throw new DavException(
          "cannot get contents", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, se);
    }
  }

  @Override
  public boolean isCollection() {
    return false;
  }
}
