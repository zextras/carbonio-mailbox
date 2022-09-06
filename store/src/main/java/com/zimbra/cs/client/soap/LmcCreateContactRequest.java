// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapParseException;
import com.zimbra.cs.client.*;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LmcCreateContactRequest extends LmcSoapRequest {

  private LmcContact mContact;

  /**
   * This method only sends the parameters from contact that the SOAP protocol will accept. That
   * means folder ID, tags, and attributes. Flags are currently ignored.
   *
   * @param c - contact to create
   */
  public void setContact(LmcContact c) {
    mContact = c;
  }

  public LmcContact getContact() {
    return mContact;
  }

  protected Element getRequestXML() throws LmcSoapClientException {
    Element request = DocumentHelper.createElement(MailConstants.CREATE_CONTACT_REQUEST);
    Element newCN = DomUtil.add(request, MailConstants.E_CONTACT, "");
    LmcSoapRequest.addAttrNotNull(newCN, MailConstants.A_FOLDER, mContact.getFolder());
    LmcSoapRequest.addAttrNotNull(newCN, MailConstants.A_TAGS, mContact.getTags());

    // emit contact attributes if any
    LmcContactAttr attrs[] = mContact.getAttrs();
    for (int i = 0; attrs != null && i < attrs.length; i++) addContactAttr(newCN, attrs[i]);

    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML)
      throws SoapParseException, ServiceException, LmcSoapClientException {

    LmcCreateContactResponse response = new LmcCreateContactResponse();
    LmcContact c = parseContact(DomUtil.get(responseXML, MailConstants.E_CONTACT));
    response.setContact(c);
    return response;
  }
}
