// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.soap.MailConstants;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LmcGetNoteRequest extends LmcSoapRequest {

  private String mNoteToGet;

  /**
   * Set the ID of the note to get.
   *
   * @param n - the ID of the note to get
   */
  public void setNoteToGet(String n) {
    mNoteToGet = n;
  }

  public String getNoteToGet() {
    return mNoteToGet;
  }

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(MailConstants.GET_NOTE_REQUEST);
    Element note = DomUtil.add(request, MailConstants.E_NOTE, "");
    DomUtil.addAttr(note, MailConstants.A_ID, mNoteToGet);
    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML) throws ServiceException {
    LmcGetNoteResponse response = new LmcGetNoteResponse();
    Element noteElem = DomUtil.get(responseXML, MailConstants.E_NOTE);
    response.setNote(parseNote(noteElem));
    return response;
  }
}
