// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.soap.MailConstants;
import java.util.Iterator;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LmcCheckSpellingRequest extends LmcSoapRequest {

  private String mText;

  public LmcCheckSpellingRequest(String text) {
    mText = text;
  }

  public String getText() {
    return mText;
  }

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(MailConstants.CHECK_SPELLING_REQUEST);
    request.addText(mText);
    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML) throws ServiceException {
    boolean isAvailable = DomUtil.getAttrBoolean(responseXML, MailConstants.A_AVAILABLE);
    LmcCheckSpellingResponse response = new LmcCheckSpellingResponse(isAvailable);

    Iterator i = responseXML.elementIterator();
    while (i.hasNext()) {
      Element misspelled = (Element) i.next();
      String word = DomUtil.getAttr(misspelled, MailConstants.A_WORD);
      String suggestions = DomUtil.getAttr(misspelled, MailConstants.A_SUGGESTIONS);
      response.addMisspelled(word, suggestions.split(","));
    }

    return response;
  }
}
