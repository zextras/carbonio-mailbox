// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.milter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

final class NioMilterEncoder extends ProtocolEncoderAdapter {

    @Override
    public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) {
        MilterPacket packet = (MilterPacket) msg;

        IoBuffer buffer = IoBuffer.allocate(4 + packet.getLength(), false);
        buffer.setAutoExpand(true);
        buffer.putInt(packet.getLength());
        buffer.put(packet.getCommand());
        byte[] data = packet.getData();
        if (data != null && data.length > 0) {
            buffer.put(data);
        }
        buffer.flip();
        out.write(buffer);
    }
}
