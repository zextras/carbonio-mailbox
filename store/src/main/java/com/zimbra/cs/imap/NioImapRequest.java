// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.io.IOException;

import org.apache.mina.filter.codec.ProtocolDecoderException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.imap.ImapParseException.ImapMaximumSizeExceededException;

final class NioImapRequest extends ImapRequest {
    private Literal literal;    // current literal data
    private int literalCount;   // remaining byte count for current literal
    private boolean complete;   // if true then request is complete

    NioImapRequest(ImapHandler handler) {
        super(handler);
    }

    boolean parse(Object obj) throws IOException, ProtocolDecoderException {
        if (literal != null) {
            parseLiteral((byte[]) obj);
        } else if (obj instanceof byte[]) {
            parseCommand(new String((byte []) obj));
        } else {
            parseCommand((String) obj);
        }
        return complete;
    }

    private void parseLiteral(byte[] b) throws IOException {
        assert b.length <= literalCount;
        literalCount -= literal.put(b, 0, b.length);
        if (literalCount <= 0) {
            assert literal.remaining() == 0;
            addPart(literal);
            literal = null;
        }
    }

    private void parseCommand(String line) throws IOException, ProtocolDecoderException {
        addPart(line);
        LiteralInfo li = LiteralInfo.parse(line); // literal format is already validated in decoder
        if (li != null) {
            literalCount = li.getCount();
            literal = Literal.newInstance(literalCount, isAppend());
            if (li.count <= 0) { // empty literal
                addPart(literal);
                complete = true;
            } else if (li.isBlocking()) {
                mHandler.sendContinuation("send literal data");
            }
        } else {
            complete = true;
        }
    }

    @Override
    public Literal readLiteral() throws ImapParseException {
        skipChar('{');
        if (index + 1 >= parts.size()) {
            throw new ImapParseException(tag, "no next literal");
        }
        Part part = parts.get(index + 1);
        index += 2;
        offset = 0;
        return part.getLiteral();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Part part : parts) {
            sb.append(part);
            if (part.isString()) {
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }

    public void checkSize(long size) throws ImapParseException {
        int maxLiteralSize = Integer.MAX_VALUE;
        if (isAppend()) {
            try {
                long msgLimit = mHandler.getConfig().getMaxMessageSize();
                if ((msgLimit != 0 /* 0 means unlimited */) && (msgLimit < maxLiteralSize)
                        && (size > msgLimit)) {
                    throwSizeExceeded("message");
                }
            } catch (ServiceException se) {
                ZimbraLog.imap.warn("unable to check zimbraMtaMaxMessageSize", se);
            }
        }
        if (size >= mHandler.config.getMaxRequestSize()){
            throwSizeExceeded("request");
        }
    }

    private void throwSizeExceeded(String exceededType) throws ImapParseException {
        if (tag == null && index == 0 && offset == 0) {
            tag = readTag(); rewind();
        }
        throw new ImapMaximumSizeExceededException(tag, exceededType);
    }

}
