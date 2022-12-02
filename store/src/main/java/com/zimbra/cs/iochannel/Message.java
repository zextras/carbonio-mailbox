// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.iochannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;

/**
 * Each Message contains payload and recipient.  MessageChannel framework locates
 * the home server for the recipient, serializes the message, then sends the packet
 * to the server where the recipient is homed.
 *
 * @author jylee
 *
 */
public abstract class Message {

    /**
     * appId is a unique identifier to let MessageChannel to find
     * correct message instance for unmarshalling.
     */
    public abstract String getAppId();

    /**
     * AccountId of the recipient.
     */
    public abstract String getRecipientAccountId();

    /**
     * Size in number of bytes.  It's a hint used during marshalling.  When the
     * exact size is unknown use a number greater than actual size
     */
    protected abstract int size();

    /**
     * Serializes the payload into ByteBuffer.
     */
    protected abstract void serialize(ByteBuffer out) throws IOException;

    /**
     * Constructs the message by parsing the payload in ByteBuffer.
     */
    protected abstract Message construct(ByteBuffer in) throws IOException;

    /**
     * Returns MessageHandler instance.
     * @return
     */
    public abstract MessageHandler getHandler();

    /**
     * MessageHandler is an object that gets called when a Message is received
     * from a peer server on MessageChannel.  Each Message subclass must have
     * its own unique MessageHandler object that knows how to deal with
     * the Message.
     */
    public interface MessageHandler {
        public void handle(Message m, String clientId);
    }

    private static final HashMap<String,Message> messages = new HashMap<String,Message>();

    static {
        registerMessage(new CrossServerNotification());
        registerMessage(new MailboxNotification());
    }

    public static void registerMessage(Message m) {
        synchronized (messages) {
            messages.put(m.getAppId(), m);
        }
    }

    public static Message create(ByteBuffer buffer) throws IOException {
        int len = buffer.getInt();
        byte[] appId = new byte[len];
        buffer.get(appId);
        String appIdStr = new String(appId, "UTF-8");
        Message m = messages.get(appIdStr);
        if (m != null) {
            return m.construct(buffer);
        }
        throw MessageChannelException.NoSuchMessage(appIdStr);
    }

    private int messageSize() throws IOException {
        // length of string in double byte + 4 byte int for length
        return size() + getAppId().length() * 2 + 4 + padding;
    }

    public ByteBuffer serialize() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(messageSize());
        writeString(buffer, getAppId());
        serialize(buffer);
        buffer.flip();
        return buffer;
    }

    protected Message() {
        this(null);
    }

    protected Message(Charset c) {
        charset = c != null ? c : Charset.forName("UTF-8");
    }

    protected ByteBuffer encode(String str) throws IOException {
        return charset.newEncoder().encode(CharBuffer.wrap(str));
    }

    protected String decode(ByteBuffer buffer) throws IOException {
        return charset.newDecoder().decode(buffer).toString();
    }

    protected void writeString(ByteBuffer buffer, String str) throws IOException {
        ByteBuffer encodedBuffer = encode(str);
        buffer.putInt(encodedBuffer.limit());
        buffer.put(encodedBuffer);
    }

    protected String readString(ByteBuffer buffer) throws IOException {
        int len = buffer.getInt();
        ByteBuffer sub = buffer.slice();
        sub.limit(len);
        buffer.position(buffer.position() + len);
        return decode(sub);
    }

    private static final int padding = 256;
    protected static Log log = LogFactory.getLog("iochannel");
    protected final Charset charset;
}
