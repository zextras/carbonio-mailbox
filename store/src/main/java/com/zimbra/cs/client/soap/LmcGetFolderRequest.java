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

public class LmcGetFolderRequest extends LmcSoapRequest {

  private String mFolderID;

  public void setFolderToGet(String f) {
    mFolderID = f;
  }

  public String getFolderToGet() {
    return mFolderID;
  }

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(MailConstants.GET_FOLDER_REQUEST);
    if (mFolderID != null) {
      Element folder = DomUtil.add(request, MailConstants.E_FOLDER, "");
      DomUtil.addAttr(folder, MailConstants.A_FOLDER, mFolderID);
    }
    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML) throws ServiceException {
    // LmcGetFolderResponse always has the 1 top level folder
    Element fElem = DomUtil.get(responseXML, MailConstants.E_FOLDER);
    LmcFolder f = parseFolder(fElem);

    LmcGetFolderResponse response = new LmcGetFolderResponse();
    response.setRootFolder(f);
    return response;
  }
}
