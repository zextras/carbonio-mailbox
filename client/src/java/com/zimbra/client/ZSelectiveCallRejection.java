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

public class ZSelectiveCallRejection extends ZCallFeature {

    private List<String> mRejectFrom;

    public ZSelectiveCallRejection(String name) {
        super(name);
        mRejectFrom = new ArrayList<String>();        
    }

    public List<String> getRejectFrom() {
        return new ArrayList<String>(mRejectFrom);
    }

    public synchronized void setRejectFrom(List<String> list) {
        mRejectFrom.clear();
        mRejectFrom.addAll(list);
    }

    public synchronized void assignFrom(ZCallFeature that) {
        super.assignFrom(that);
        if (that instanceof ZSelectiveCallRejection) {
            this.mRejectFrom = new ArrayList<String>(((ZSelectiveCallRejection)that).mRejectFrom);
        }
    }

    synchronized void fromElement(Element element) throws ServiceException {
        super.fromElement(element);
        mRejectFrom = new ArrayList<String>();
        for (Element fromEl : element.listElements(VoiceConstants.E_PHONE)) {
            mRejectFrom.add(fromEl.getAttribute(VoiceConstants.A_PHONE_NUMBER));
        }
    }

    void toElement(Element element) throws ServiceException {
        super.toElement(element);
	for (String name : mRejectFrom) {
            Element fromEl = element.addElement(VoiceConstants.E_PHONE);
            fromEl.addAttribute(VoiceConstants.A_PHONE_NUMBER, name);
            fromEl.addAttribute(VoiceConstants.A_ACTIVE, "true");
        }
    }
}
