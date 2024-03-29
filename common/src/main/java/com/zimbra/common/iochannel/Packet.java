// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.iochannel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.zimbra.common.iochannel.Client.PeerServer;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import java.nio.charset.StandardCharsets;

/**
 * Packet is smallest unit of data transmitted among the machines
 * in iochannel cluster.  There is a small header field with magic
 * identification, id of the sender, and the payload.
 *
 * @author jylee
 *
 */
public class Packet {

    public static Packet create(String clientId, PeerServer server, ByteBuffer message) {
        return new Packet(clientId, server, message);
    }

    public void setDestination(PeerServer s) {
        destination = s;
    }

    public void setContent(ByteBuffer p) {
        payload[1] = p;
    }

    public String getHeader() throws IOException {
        return new String(payload[0].array(), StandardCharsets.UTF_8);
    }

    public ByteBuffer getContent() {
        return payload[1].asReadOnlyBuffer();
    }

    public PeerServer getDestination() {
        return destination;
    }

    public ByteBuffer[] getPayload() {
        return payload;
    }

    public boolean hasRemaining() {
        return payload[0].remaining() > 0 || payload[1].remaining() > 0;
    }

    private Packet(String clientId, PeerServer server, ByteBuffer message) {
        byte[] clientIdBytes;
      clientIdBytes = clientId.getBytes(StandardCharsets.UTF_8);
      ByteBuffer header = ByteBuffer.allocate(maxHeaderSize);
        header.putInt(magic).putLong(clientId.length()).putLong(message.remaining()).put(clientIdBytes).flip();
        payload = new ByteBuffer[2];
        payload[0] = header;
        payload[1] = message;
        destination = server;
    }

    private Packet(byte[] header, byte[] content) {
        payload = new ByteBuffer[2];
        payload[0] = ByteBuffer.wrap(header);
        payload[1] = ByteBuffer.wrap(content);
    }

    public static Packet fromBuffer(ByteBuffer buffer) throws IOChannelException {
        if (buffer.position() < minimumHeaderSize) {
            return null;
        }
        ByteBuffer copy = buffer.duplicate();
        copy.flip();
        // sanity check
        if (copy.getInt() != Packet.magic) {
            log.warn("magic mismatch");
            // it's corrupted data or beginning part has been lost.
            // discard what's been read and hope that the next segment
            // starts from the beginning of a new message.
            buffer.clear();
            return null;
        }

        long headerLen = copy.getLong();
        long contentLen = copy.getLong();
        if (copy.remaining() < headerLen + contentLen) {
            // not enough space in the buffer to read the Packet fully.
            byte[] headerBuf = new byte[(int)headerLen];
            copy.get(headerBuf);
            throw IOChannelException.PacketTooBig(new String(headerBuf));
        }
        // fully read the current message
        byte[] headerBuf = new byte[(int)headerLen];
        byte[] contentBuf = new byte[(int)contentLen];
        copy.get(headerBuf);
        copy.get(contentBuf);
        buffer.flip().position(copy.position());
        buffer.compact();
        if (log.isDebugEnabled()) {
          log.debug("msg from %s '%s'", new String(headerBuf, StandardCharsets.UTF_8), new String(contentBuf,
              StandardCharsets.UTF_8));
        }
        return new Packet(headerBuf, contentBuf);
    }

    private PeerServer destination;
    private final ByteBuffer[] payload;

    private static final Log log = LogFactory.getLog("iochannel");
    private static final int minimumHeaderSize = (Integer.SIZE + 2 * Long.SIZE) / 8;  // one integer and two longs;
    public static final int magic = 0xdeadbeef;
    public static final int maxHeaderSize = 1024;
    public static final int maxMessageSize = 10240;
}

