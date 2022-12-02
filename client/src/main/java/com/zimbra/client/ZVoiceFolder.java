// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.common.service.ServiceException;

import java.util.Map;
import java.util.HashMap;

public class ZVoiceFolder extends ZFolder {

    private static Map<String, Integer> mSortMap;
    static {
        mSortMap = new HashMap<String, Integer>();
        mSortMap.put(VoiceConstants.FNAME_PLACEDCALLS, 5);
        mSortMap.put(VoiceConstants.FNAME_ANSWEREDCALLS, 4);
        mSortMap.put(VoiceConstants.FNAME_MISSEDCALLS, 3);
        mSortMap.put(VoiceConstants.FNAME_VOICEMAILINBOX, 1);
        mSortMap.put(VoiceConstants.FNAME_TRASH, 2);
    }

    public ZVoiceFolder(Element e, ZFolder parent, ZMailbox mailbox) throws ServiceException {
        super(e, parent, mailbox);
    }

    protected ZFolder createSubFolder(Element element) throws ServiceException {
        return new ZVoiceFolder(element, this, getMailbox());
    }

    public int compareTo(Object obj) {
        if (!(obj instanceof ZVoiceFolder))
            return 0;
        ZFolder other = (ZFolder) obj;
        int valueA = mSortMap.get(getName());
        int valueB = mSortMap.get(other.getName());
        return valueA - valueB;
    }
}
