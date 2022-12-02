// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import junit.framework.Assert;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoSession;
import org.junit.Test;

/**
 * Unit test for {@link NioOutputStream}.
 *
 * @author ysasaki
 */
public final class NioOutputStreamTest {

    @Test
    public void writeByte() throws Exception {
        DummySession session = new DummySession();
        TestIoHandler handler = new TestIoHandler();
        session.setHandler(handler);
        NioOutputStream out = new NioOutputStream(session, 10, Integer.MAX_VALUE, Integer.MAX_VALUE);
        out.write('1');
        out.write('2');
        out.write('3');
        out.write('4');
        out.write('5');
        out.write('6');
        out.write('7');
        out.write('8');
        out.write('9');
        out.write('0');
        out.write('1');
        out.close();
        Assert.assertEquals(2, handler.getWriteCount());
        Assert.assertEquals("12345678901", handler.toString());
    }

    @Test
    public void writeByteArray() throws Exception {
        DummySession session = new DummySession();
        TestIoHandler handler = new TestIoHandler();
        session.setHandler(handler);
        NioOutputStream out = new NioOutputStream(session, 10, Integer.MAX_VALUE, Integer.MAX_VALUE);
        byte[] b = new byte[] {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1'};
        out.write(b, 0, 10);
        out.write(new byte[] {' '}, 0, 1);
        out.write(b, 0, 11);
        out.close();
        Assert.assertEquals(3, handler.getWriteCount());
        Assert.assertEquals("1234567890 12345678901", handler.toString());
    }

    @Test
    public void writeString() throws Exception {
        DummySession session = new DummySession();
        TestIoHandler handler = new TestIoHandler();
        session.setHandler(handler);
        NioOutputStream out = new NioOutputStream(session, 10, Integer.MAX_VALUE, Integer.MAX_VALUE);
        out.write("1234567890");
        out.write(" ");
        out.write("12345678901");
        Assert.assertEquals(3, handler.getWriteCount());
        Assert.assertEquals("1234567890 12345678901", handler.toString());
        out.close();
    }

    private static final class TestIoHandler extends IoHandlerAdapter {
        private int writes = 0;
        private StringBuilder out = new StringBuilder();

        @Override
        public void messageSent(IoSession session, Object message) {
            writes++;
            IoBuffer buf = (IoBuffer) message;
            while (buf.hasRemaining()) {
                out.append((char) buf.get());
            }
        }

        int getWriteCount() {
            return writes;
        }

        @Override
        public String toString() {
            return out.toString();
        }
    }
}
