// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailclient.util.Io;
import com.zimbra.cs.mailclient.util.Ascii;
import com.zimbra.cs.mailclient.util.LimitInputStream;
import com.zimbra.cs.mailclient.ParseException;
import com.zimbra.cs.mailclient.MailInputStream;
import com.zimbra.cs.mailclient.MailException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.EOFException;

/**
 * An input stream for reading IMAP response data.
 */
public final class ImapInputStream extends MailInputStream {
    private final ImapConnection connection;
    private final ImapConfig config;
    private final ByteArrayOutputStream baos;

    private static final boolean DEBUG = false;

    public ImapInputStream(InputStream is, ImapConnection connection) {
        super(is);
        this.connection = connection;
        config = connection.getImapConfig();
        baos = new ByteArrayOutputStream();
    }

    public ImapInputStream(InputStream is, ImapConnection connection, Log log) {
        super(is, log);
        this.connection = connection;
        config = connection.getImapConfig();
        baos = new ByteArrayOutputStream();
    }

    public ImapInputStream(InputStream is, ImapConfig config) {
        super(is);
        connection = null;
        this.config = config;
        baos = new ByteArrayOutputStream();
    }

    public Atom readAtom() throws IOException {
        skipSpaces();
        String s = readChars(Chars.ATOM_CHARS);
        if (s.length() == 0) {
            throw new ParseException("Zero-length atom");
        }
        if (DEBUG) pd("readAtom: %s", s);
        return new Atom(s);
    }

    public Atom readFlag() throws IOException {
        skipSpaces();
        sbuf.setLength(0);
        if (peek() == '\\') {
            sbuf.append((char) read());
            if (peek() == '*') {
                sbuf.append((char) read());
                return new Atom(sbuf.toString());
            }
        }
        int len = sbuf.length();
        while (Chars.isAtomChar(peekChar())) {
            sbuf.append((char) read());
        }
        if (sbuf.length() - len == 0) {
            throw new ParseException(
                "Invalid flag character '" + Ascii.pp((byte) peek()) + "'");
        }
        return new Atom(sbuf.toString());
    }

    public String readString() throws IOException {
        return readStringData().toString();
    }

    public ImapData readStringData() throws IOException {
        ImapData as = readAStringData();
        if (!as.isString()) {
            throw new ParseException("Expected STRING but got " + as);
        }
        return as;
    }

    public String readNString() throws IOException {
        ImapData ns = readNStringData();
        return ns.isNil() ? null : ns.toString();
    }

    public ImapData readNStringData() throws IOException {
        ImapData as = readAStringData();
        if (!as.isNString()) {
            throw new ParseException("Expected NIL or STRING, but got: " + as);
        }
        return as;
    }

    public Object readFetchData() throws IOException {
        DataHandler handler = getDataHandler();
        if (handler == null) {
            return readNStringData();
        }
        ImapData as = peek() == '{' ? readLiteral(false) : readNStringData();
        try {
            return handler.handleData(as);
        } catch (Throwable e) {
            throw new MailException("Exception in data handler", e);
        } finally {
            if (as.isLiteral()) {
                skipRemaining(as.getInputStream());
            }
        }
    }

    private void skipRemaining(InputStream is) throws IOException {
        while (is.skip(8191) > 0) ;
    }

    private DataHandler getDataHandler() {
        return connection != null ? connection.getDataHandler() : null;
    }

    public String readAString() throws IOException {
        return readAStringData().toString();
    }

    public ImapData readAStringData() throws IOException {
        skipSpaces();
        ImapData as;
        String s;
        switch (peekChar()) {
        case '"':
            as = readQuoted();
            break;
        case '{':
            as = readLiteral(true);
            break;
        default:
            s = readChars(Chars.ASTRING_CHARS);
            if (s.length() == 0) {
                throw new ParseException("Zero-length atom");
            }
            as = new Atom(s);
        }
        if (DEBUG) pd("readAString: %s (%s)", as, as.getType());
        return as;
    }

    public boolean isNumber() throws IOException {
        return !isEOF() && Chars.isNumber(peekChar());
    }

    public long readNZNumber() throws IOException {
        return readNumber(true);
    }

    public long readNumber() throws IOException {
        return readNumber(false);
    }

    private long readNumber(boolean nz) throws IOException {
        skipSpaces();
        String s = readChars(Chars.NUMBER_CHARS);
        if (s.length() == 0) {
            throw new ParseException("Zero-length number");
        }
        long n = Chars.getNumber(s);
        if (n == -1) {
            throw new ParseException("Invalid number format: " + s);
        }
        if (nz && n == 0) {
            throw new ParseException("Expected non-zero number but got " + s);
        }
        if (DEBUG) pd("readNumber: %d", n);
        return n;
    }

    public Quoted readQuoted() throws IOException {
        skipChar('"');
        sbuf.setLength(0);
        int c;
        while ((c = read()) != '"') {
            switch (c) {
            case '\r': case '\n':
                throw new ParseException(
                    "Unexpected end of line while reading QUOTED string");
            case -1:
                throw new EOFException(
                    "Unexpected end of stream while reading QUOTED string");
            case '\\':
                c = readChar();
            }
            sbuf.append((char) c);
        }
        return new Quoted(sbuf.toString());
    }

    private Literal readLiteral(boolean cache) throws IOException {
        skipChar('{');
        long len = readNumber();
        if (len > Integer.MAX_VALUE) {
            throw new ParseException("Literal size too large: " + len);
        }
        if (DEBUG) pd("readLiteral: size = %d", len);
        skipSpaces();
        skipChar('}');
        skipCRLF();
        // If data not cached, then caller handles suspend/resume trace
        return readLiteral((int) len, cache);
    }

    private Literal readLiteral(int len, boolean cache) throws IOException {
        if (!cache) {
            return new Literal(new LimitInputStream(in, len), len);
        }
        if (len <= config.getMaxLiteralMemSize()) {
            // Cache literal data in memory
            byte[] b = new byte[len];
            Io.readFully(in, b);
            return new Literal(b);
        }
        // Otherwise, use temporary file for literal data, which will be
        // automatically cleaned up when the ImapResponse is finished.
        File f = File.createTempFile("lit", null, config.getLiteralDataDir());
        f.deleteOnExit();
        OutputStream os = new FileOutputStream(f);
        try {
            Io.copyBytes(in, os, len);
        } finally {
            os.close();
        }
        return new Literal(f, true);
    }

    public String readText() throws IOException {
        if (isEOL()) {
            return "";
        }
        // bug 43997: Handle possible UTF8 encoded response text in greeting.
        baos.reset();
        do {
            baos.write(read());
        } while (!isEOL());
        return baos.toString("UTF8");
    }

    public String readText(char delim) throws IOException {
        return readText(String.valueOf(delim));
    }

    public String readText(String delims) throws IOException {
        sbuf.setLength(0);
        char c = 0;
        while (!isEOF() && delims.indexOf(c = peekChar()) < 0) {
            if (!Chars.isTextChar(c)) {
                throw new ParseException(
                    "Unexpected character '" + Ascii.pp((byte) c) +
                    "' while reading TEXT string");
            }
            sbuf.append((char) read());
        }
        if (sbuf.length() == 0) {
            throw new ParseException(
                "Invalid text character '" + Ascii.pp((byte) c) + "'");
        }
        return sbuf.toString();
    }

    public String readChars(boolean[] chars) throws IOException {
        sbuf.setLength(0);
        while (chars[peekChar()]) {
            sbuf.append((char) read());
        }
        return sbuf.toString();
    }

    public void skipOptionalChar(char expectedChar) throws IOException {
        char c = peekChar();
        if (c == expectedChar) {
            readChar();
        } 
    }

    public void skipChar(char expectedChar) throws ParseException, IOException {
        char c = readChar();
        if (c != expectedChar) {
            throw new ParseException(
                "Unexpected character '" + Ascii.pp((byte) c) +
                "' (expecting '" + Ascii.pp((byte) expectedChar) + "')");
        }
    }

    public void skipNil() throws IOException {
        Atom atom = readAtom();
        if (!atom.isNil()) {
            throw new ParseException("Expecting NIL but got " + atom);
        }
    }

    public void skipCRLF() throws IOException {
        try {
            skipSpaces();
            skipChar('\r');
            skipChar('\n');
        } catch (ParseException pe) {
            //parse exception; read until the end of line so we can get meaningful debug
            ZimbraLog.imap_client.error("ParseException reading EOL", pe);
            //do nothing, just advancing to EOL. If we never find a \n then stream is prematurely closed or server is noncompliant
            while (readChar() != '\n') {
            }
        }
    }

    /**
     * If next character in stream matches specified character then read it
     * and return true. Otherwise, return false.
     *
     * @param c the expected character
     * @return true if next character matches, false otherwise
     * @throws IOException an an I/O error occurs
     */
    public boolean match(char c) throws IOException {
        if (peek() == c) {
            read();
            return true;
        }
        return false;
    }

    public void skipSpaces() throws IOException {
        while (match(' ')) ;
    }

    public boolean isEOL() throws IOException {
        return peek() == '\r';
    }

    private static void pd(String fmt, Object... args) {
        System.out.print("[DEBUG] ");
        System.out.printf(fmt, args);
        System.out.println();
    }
}
