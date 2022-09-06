// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavContext.RequestProp;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.carddav.Filter;
import com.zimbra.cs.dav.resource.AddressObject;
import com.zimbra.cs.dav.resource.AddressbookCollection;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.service.DavResponse;
import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.Element;

/*
 * draft-daboo-carddav-02 section 10.5
 *
 *        <!ELEMENT addressbook-query ((DAV:allprop |
 *                                      DAV:propname |
 *                                      DAV:prop)?, filter)>
 */
public class AddressbookQuery extends Report {
  public void handle(DavContext ctxt) throws DavException, ServiceException {
    DavResource rsc = ctxt.getRequestedResource();
    if (!(rsc instanceof AddressbookCollection))
      throw new DavException(
          "not an addressbook resource", HttpServletResponse.SC_BAD_REQUEST, null);

    Element query = ctxt.getRequestMessage().getRootElement();
    if (!query.getQName().equals(DavElements.CardDav.E_ADDRESSBOOK_QUERY))
      throw new DavException(
          "msg " + query.getName() + " is not addressbook-query",
          HttpServletResponse.SC_BAD_REQUEST,
          null);
    Element f = query.element(DavElements.CardDav.E_FILTER);
    if (f == null)
      throw new DavException(
          "msg " + query.getName() + " is missing filter",
          HttpServletResponse.SC_BAD_REQUEST,
          null);
    Filter filter = new Filter.PropFilter((Element) f.elementIterator().next());
    Collection<AddressObject> contacts = filter.match(ctxt, ((AddressbookCollection) rsc));
    RequestProp reqProp = ctxt.getRequestProp();
    DavResponse resp = ctxt.getDavResponse();
    resp.createResponse(ctxt);
    for (AddressObject c : contacts) {
      resp.addResource(ctxt, c, reqProp, false);
    }
  }
}
