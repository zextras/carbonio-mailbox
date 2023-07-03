// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.net.SocketAddress;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.imap.NioImapDecoder.TooBigLiteralException;
import com.zimbra.cs.imap.NioImapDecoder.TooLongLineException;

/**
 * Unit test for {@link NioImapDecoder}.
 *
 * @author ysasaki
 */
public final class NioImapDecoderTest {
    private static final CharsetEncoder CHARSET = Charsets.ISO_8859_1.newEncoder();
    private static final IoBuffer IN = IoBuffer.allocate(1024).setAutoExpand(true);

    private TestImapConfig imapConfig;
    private NioImapDecoder decoder;
    private ProtocolCodecSession session;

    private static final class TestImapConfig extends ImapConfig {
        private long maxMessageSize;
        TestImapConfig(boolean ssl) throws ServiceException {
            super(ssl);
            maxMessageSize = -1L; /* means "no limit" */
        }

        public void setMaxMessageSize(long l) {
            maxMessageSize = l;
        }

        @Override
        public long getMaxMessageSize() {
            return maxMessageSize;
        }

        @Override
        public int getMaxRequestSize() {
            return 1024;
        }

        @Override
        public int getWriteChunkSize() {
            return 1024;
        }
    }

    @BeforeEach
    public void setUp() throws ServiceException {
        imapConfig = new TestImapConfig(false);
        decoder = new NioImapDecoder(imapConfig);
        session = new ProtocolCodecSession();
        session.setTransportMetadata(new DefaultTransportMetadata("test", "test", false, true, // Enable fragmentation
                SocketAddress.class, IoSessionConfig.class, Object.class));
    }

 @Test
 void decodeLine() throws Exception {
  IN.clear().putString("CRLF\r\n", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("CRLF", session.getDecoderOutputQueue().poll());

  IN.clear().putString("ABC\r\nDEF\r\nHIJ\r\n", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("ABC", session.getDecoderOutputQueue().poll());
  assertEquals("DEF", session.getDecoderOutputQueue().poll());
  assertEquals("HIJ", session.getDecoderOutputQueue().poll());

  IN.clear().putString("CR\r and LF\n", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("CR\r and LF", session.getDecoderOutputQueue().poll());

  IN.clear().putString("A\r\r\n", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("A\r", session.getDecoderOutputQueue().poll());

  IN.clear().putString("A\r\n\r\nB\r\n", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("A", session.getDecoderOutputQueue().poll());
  assertEquals("", session.getDecoderOutputQueue().poll());
  assertEquals("B", session.getDecoderOutputQueue().poll());

  IN.clear().putString("not EOL yet...", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals(0, session.getDecoderOutputQueue().size());

  IN.clear().putString("CRLF\r\n", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("not EOL yet...CRLF", session.getDecoderOutputQueue().poll());

  assertEquals(0, session.getDecoderOutputQueue().size());
 }

 @Test
 void decodeLiteral() throws Exception {
  IN.clear().putString("A003 APPEND Drafts (\\Seen \\Draft $MDNSent) CATENATE (TEXT {10}\r\n", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("A003 APPEND Drafts (\\Seen \\Draft $MDNSent) CATENATE (TEXT {10}",
    session.getDecoderOutputQueue().poll());

  byte[] literal = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
  IN.clear().put(literal).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertArrayEquals(literal, (byte[]) session.getDecoderOutputQueue().poll());

  assertEquals(0, session.getDecoderOutputQueue().size());
 }

 @Test
 void maxLineLength() throws Exception {
  IN.clear().fill(1024).putString("\r\nrecover\r\n", CHARSET).flip();
  try {
   decoder.decode(session, IN, session.getDecoderOutput());
   fail("decoder.decode did NOT throw TooLongLineException as expected");
  } catch (TooLongLineException expected) {
  }
  assertEquals(0, session.getDecoderOutputQueue().size());

  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("recover", session.getDecoderOutputQueue().poll());
 }

 @Test
 void badLiteral() throws Exception {
  IN.clear().putString("XXX {-1}\r\n", CHARSET).flip();
  try {
   decoder.decode(session, IN, session.getDecoderOutput());
   fail();
  } catch (TooBigLiteralException expected) {
  }
  assertEquals(0, session.getDecoderOutputQueue().size());

  IN.clear().putString("XXX {2147483648}\r\n", CHARSET).flip();
  try {
   decoder.decode(session, IN, session.getDecoderOutput());
   fail();
  } catch (TooBigLiteralException expected) {
  }
  assertEquals(0, session.getDecoderOutputQueue().size());
 }

 @Test
 void maxLiteralSize() throws Exception {
  imapConfig.setMaxMessageSize(1024L);
  IN.clear().putString("XXX {1025}\r\nrecover\r\n", CHARSET).flip();
  try {
   decoder.decode(session, IN, session.getDecoderOutput());
   fail();
  } catch (TooBigLiteralException expected) {
   assertEquals("XXX {1025}", expected.getRequest());
  }
  assertEquals(0, session.getDecoderOutputQueue().size());

  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("recover", session.getDecoderOutputQueue().poll());

  IN.clear().putString("XXX {1025+}\r\n", CHARSET).fill(1025).putString("recover\r\n", CHARSET).flip();
  try {
   decoder.decode(session, IN, session.getDecoderOutput());
   fail();
  } catch (TooBigLiteralException expected) {
  }
  assertEquals(0, session.getDecoderOutputQueue().size());

  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("recover", session.getDecoderOutputQueue().poll());
 }

 @Test
 void emptyLiteral() throws Exception {
  IN.clear().putString("A003 APPEND Drafts {0}\r\n", CHARSET).flip();
  decoder.decode(session, IN, session.getDecoderOutput());
  assertEquals("A003 APPEND Drafts {0}", session.getDecoderOutputQueue().poll());
  assertEquals(0, session.getDecoderOutputQueue().size());
 }
}
