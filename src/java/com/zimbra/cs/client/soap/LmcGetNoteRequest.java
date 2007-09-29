/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2004, 2005, 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.client.soap;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

import com.zimbra.soap.DomUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.mail.MailService;

public class LmcGetNoteRequest extends LmcSoapRequest {

    private String mNoteToGet;
    

    /**
     * Set the ID of the note to get.
     * @param n - the ID of the note to get
     */
    public void setNoteToGet(String n) { mNoteToGet = n; }
    
    public String getNoteToGet() { return mNoteToGet; }

    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(MailService.GET_NOTE_REQUEST);  
        Element note = DomUtil.add(request, MailService.E_NOTE, "");
        DomUtil.addAttr(note, MailService.A_ID, mNoteToGet);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML) 
        throws ServiceException
    {
        LmcGetNoteResponse response = new LmcGetNoteResponse();
        Element noteElem = DomUtil.get(responseXML, MailService.E_NOTE);
        response.setNote(parseNote(noteElem));
        return response;
    }

}
