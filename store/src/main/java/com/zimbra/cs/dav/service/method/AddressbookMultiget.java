// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.HttpUtil;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavContext.RequestProp;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.resource.AddressObject;
import com.zimbra.cs.dav.resource.AddressbookCollection;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.resource.UrlNamespace;
import com.zimbra.cs.dav.service.DavResponse;
import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.Element;

public class AddressbookMultiget extends Report {
  @Override
  public void handle(DavContext ctxt) throws ServiceException, DavException {
    Element query = ctxt.getRequestMessage().getRootElement();
    if (!query.getQName().equals(DavElements.CardDav.E_ADDRESSBOOK_MULTIGET))
      throw new DavException(
          "msg " + query.getName() + " is not addressbook-multiget",
          HttpServletResponse.SC_BAD_REQUEST,
          null);

    DavResponse resp = ctxt.getDavResponse();
    DavResource reqResource = ctxt.getRequestedResource();
    if (!(reqResource instanceof AddressbookCollection))
      throw new DavException(
          "requested resource is not an addressbook collection",
          HttpServletResponse.SC_BAD_REQUEST,
          null);
    RequestProp reqProp = ctxt.getRequestProp();
    for (Object obj : query.elements(DavElements.E_HREF)) {
      if (obj instanceof Element) {
        String href = ((Element) obj).getText();
        URI uri = URI.create(href);
        String[] fragments = HttpUtil.getPathFragments(uri);
        if (uri.getPath().toLowerCase().endsWith(AddressObject.VCARD_EXTENSION)) {
          // double encode the last fragment
          fragments[fragments.length - 1] =
              HttpUtil.urlEscapeIncludingSlash(fragments[fragments.length - 1]);
        }
        uri = HttpUtil.getUriFromFragments(fragments, uri.getQuery(), true, false);
        href = uri.getPath();
        DavResource rs = UrlNamespace.getResourceAtUrl(ctxt, href);
        if (rs != null) resp.addResource(ctxt, rs, reqProp, false);
      }
    }
  }
}
