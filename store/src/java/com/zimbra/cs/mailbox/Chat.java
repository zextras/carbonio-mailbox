// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.StagedBlob;

public class Chat extends Message {

    Chat(Mailbox mbox, UnderlyingData ud) throws ServiceException {
        this(mbox, ud, false);
    }
    
    /**
     * this one will call back into decodeMetadata() to do our initialization
     *
     * @param mbox
     * @param ud
     * @throws ServiceException
     */
    Chat(Mailbox mbox, UnderlyingData ud, boolean skipCache) throws ServiceException {
        super(mbox, ud, skipCache);
        if (mData.type != Type.CHAT.toByte()) {
            throw new IllegalArgumentException();
        }
        if (mData.parentId < 0) {
            mData.parentId = -mId;
        }
    }

    static class ChatCreateFactory extends MessageCreateFactory {
        @Override
        Message create(Mailbox mbox, UnderlyingData data) throws ServiceException {
            return new Chat(mbox, data);
        }

        @Override
        Type getType() {
            return Type.CHAT;
        }
    }

    static Chat create(int id, Folder folder, ParsedMessage pm, StagedBlob staged, boolean unread, int flags, Tag.NormalizedTags ntags)
    throws ServiceException {
        return (Chat) Message.createInternal(id, folder, null, pm, staged, unread, flags, ntags, null, true, null, null, new ChatCreateFactory());
    }

    @Override boolean isMutable() { return true; }
}
