// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.iochannel;

import java.io.IOException;

public class IOChannelException extends IOException {

    private static final long serialVersionUID = -506129145508295776L;

    public enum Code {
        NoSuchPeer, PacketTooBig, ChannelClosed, Error
    };

    private final Code errorCode;

    public IOChannelException(Code c, String msg) {
        super(msg);
        errorCode = c;
    }

    public Code getCode() {
        return errorCode;
    }

    public static IOChannelException NoSuchPeer(String peerId) {
        return new IOChannelException(Code.NoSuchPeer, "no such peer " + peerId);
    }

    public static IOChannelException PacketTooBig(String header) {
        return new IOChannelException(Code.PacketTooBig, "large packet from " + header);
    }

    public static IOChannelException ChannelClosed(String channel) {
        return new IOChannelException(Code.ChannelClosed, "channel closed " + channel);
    }
}
