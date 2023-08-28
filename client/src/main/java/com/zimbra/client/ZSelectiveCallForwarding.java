// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.VoiceConstants;

import java.util.ArrayList;
import java.util.List;

public class ZSelectiveCallForwarding extends ZCallFeature {

    private List<String> mForwardFrom;

    public ZSelectiveCallForwarding(String name) {
        super(name);
        mForwardFrom = new ArrayList<String>();        
    }

    public List<String> getForwardFrom() {
        return new ArrayList<String>(mForwardFrom);
    }

    public synchronized void setForwardFrom(List<String> list) {
        mForwardFrom.clear();
        mForwardFrom.addAll(list);
    }

    public synchronized void assignFrom(ZCallFeature that) {
        super.assignFrom(that);
        if (that instanceof ZSelectiveCallForwarding) {
            this.mForwardFrom = new ArrayList<String>(((ZSelectiveCallForwarding)that).mForwardFrom);
        }
    }

    synchronized void fromElement(Element element) throws ServiceException {
        super.fromElement(element);
        mForwardFrom = new ArrayList<String>();
        for (Element fromEl : element.listElements(VoiceConstants.E_PHONE)) {
             mForwardFrom.add(fromEl.getAttribute(VoiceConstants.A_PHONE_NUMBER));
        }
    }

    void toElement(Element element) throws ServiceException {
        super.toElement(element);
        for (String name : mForwardFrom) {
            Element fromEl = element.addElement(VoiceConstants.E_PHONE);
            fromEl.addAttribute(VoiceConstants.A_PHONE_NUMBER, name);
            fromEl.addAttribute(VoiceConstants.A_ACTIVE, "true");
        }
    }
}
