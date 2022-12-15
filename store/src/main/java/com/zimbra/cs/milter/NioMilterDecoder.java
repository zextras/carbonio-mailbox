// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.milter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

final class NioMilterDecoder extends CumulativeProtocolDecoder {

    @Override
    public boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
        if (!in.prefixedDataAvailable(4)) {
            return false;
        }
        int len = in.getInt();
        byte cmd = in.get();
        byte[] data = null;
        if (len > 1) {
            data = new byte[len - 1];
            in.get(data);
        }
        MilterPacket packet = new MilterPacket(len, cmd, data);
        out.write(packet);
        return true;
    }
}
