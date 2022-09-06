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

public class LmcSaveDocumentRequest extends LmcSendMsgRequest {

  private LmcDocument mDoc;

  public void setDocument(LmcDocument doc) {
    mDoc = doc;
  }

  public LmcDocument getDocument() {
    return mDoc;
  }

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(MailConstants.SAVE_DOCUMENT_REQUEST);
    Element doc = DomUtil.add(request, MailConstants.E_DOC, "");
    LmcSoapRequest.addAttrNotNull(doc, MailConstants.A_NAME, mDoc.getName());
    LmcSoapRequest.addAttrNotNull(doc, MailConstants.A_CONTENT_TYPE, mDoc.getContentType());
    LmcSoapRequest.addAttrNotNull(doc, MailConstants.A_FOLDER, mDoc.getFolder());
    Element upload = DomUtil.add(doc, MailConstants.E_UPLOAD, "");
    LmcSoapRequest.addAttrNotNull(upload, MailConstants.A_ID, mDoc.getAttachmentId());
    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML) throws ServiceException {

    LmcSaveDocumentResponse response = new LmcSaveDocumentResponse();
    LmcDocument doc = parseDocument(DomUtil.get(responseXML, MailConstants.E_DOC));
    response.setDocument(doc);
    return response;
  }
}
