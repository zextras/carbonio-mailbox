// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.common.service.ServiceException;

public class ZPhoneAccount {
    private ZFolder folder;
    private ZPhone phone;
    private ZCallFeatures callFeatures;
	private boolean hasVoiceMail;
    private String phoneType;

	public ZPhoneAccount(Element e, ZMailbox mbox) throws ServiceException {
        phone = new ZPhone(e.getAttribute(MailConstants.A_NAME));
        folder = new ZVoiceFolder(e.getElement(MailConstants.E_FOLDER), null, mbox);
        callFeatures = new ZCallFeatures(mbox, phone, e.getElement(VoiceConstants.E_CALL_FEATURES));
		hasVoiceMail = e.getAttributeBool(VoiceConstants.E_VOICEMSG);
        try{
            phoneType = e.getAttribute(VoiceConstants.A_TYPE);
        }
        catch(ServiceException ex){
            phoneType = null;
        }

	}

    public ZFolder getRootFolder() {
        return folder;
    }

    public ZPhone getPhone() {
        return phone;
    }

    public ZCallFeatures getCallFeatures() throws ServiceException {
        callFeatures.loadCallFeatures();
        return callFeatures;
    }

	public boolean getHasVoiceMail() {
		return hasVoiceMail;
	}

    public String getPhoneType() {
        return phoneType;
    }
}
