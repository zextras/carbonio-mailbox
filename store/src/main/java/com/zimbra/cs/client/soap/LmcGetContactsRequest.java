// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.client.*;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LmcGetContactsRequest extends LmcSoapRequest {

  private LmcContactAttr mAttrs[];
  private String mIDList[];

  public void setContacts(String c[]) {
    mIDList = c;
  }

  public void setAttrs(LmcContactAttr attrs[]) {
    mAttrs = attrs;
  }

  public String[] getContacts() {
    return mIDList;
  }

  public LmcContactAttr[] getAttrs() {
    return mAttrs;
  }

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(MailConstants.GET_CONTACTS_REQUEST);

    // emit contact attributes if any
    for (int i = 0; mAttrs != null && i < mAttrs.length; i++) addContactAttr(request, mAttrs[i]);

    // emit specified contacts if any
    for (int i = 0; mIDList != null && i < mIDList.length; i++) {
      Element newCN = DomUtil.add(request, MailConstants.E_CONTACT, "");
      DomUtil.addAttr(newCN, MailConstants.A_ID, mIDList[i]);
    }

    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML)
      throws ServiceException, LmcSoapClientException {
    LmcGetContactsResponse response = new LmcGetContactsResponse();
    LmcContact cons[] = parseContactArray(responseXML);
    response.setContacts(cons);
    return response;
  }
}
