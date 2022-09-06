// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import java.util.HashMap;
import java.util.Iterator;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LmcGetInfoRequest extends LmcSoapRequest {

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(AccountConstants.GET_INFO_REQUEST);
    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML) throws ServiceException {
    HashMap prefMap = new HashMap();
    LmcGetInfoResponse response = new LmcGetInfoResponse();

    // iterate over all the elements we received
    for (Iterator it = responseXML.elementIterator(); it.hasNext(); ) {
      Element e = (Element) it.next();

      // find out what element it is and go process that
      String elementType = e.getQName().getName();
      if (elementType.equals(AccountConstants.E_NAME)) {
        response.setAcctName(e.getText());
      } else if (elementType.equals(AccountConstants.E_LIFETIME)) {
        response.setLifetime(e.getText());
      } else if (elementType.equals(AccountConstants.E_PREF)) {
        // add this preference to our map
        addPrefToMultiMap(prefMap, e);
      }
    }

    if (!prefMap.isEmpty()) response.setPrefMap(prefMap);

    return response;
  }
}
