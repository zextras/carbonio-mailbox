// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public class ImapParseException extends ImapException {
    private static final long serialVersionUID = 4675342317380797673L;

    protected String mTag;
    protected String responseCode;
    protected boolean userServerResponseNO;  /* if true use "NO" as server response, else use "BAD" */

    protected ImapParseException() {
    }

    protected ImapParseException(String tag, String message) {
        super("parse error: " + message);
        mTag = tag;
    }

    protected ImapParseException(String tag, String message, boolean no, boolean parseError) {
        super((parseError ? "parse error: " : "") + message);
        mTag = tag;
        userServerResponseNO = no;
    }

    protected ImapParseException(String tag, String code, String message, boolean parseError) {
        super((parseError ? "parse error: " : "") + message);
        mTag = tag;
        responseCode = code;
        userServerResponseNO = code != null;
    }

    protected static class ImapMaximumSizeExceededException extends ImapParseException {
        private static final long serialVersionUID = -8080429172062016010L;
        public static final String sizeExceededFmt = "maximum %s size exceeded";
        protected ImapMaximumSizeExceededException(String tag, String code, String exceededType) {
            super(tag, code,
                    String.format(sizeExceededFmt, exceededType),
                    false /* don't prefix parse error: */);
        }
        protected ImapMaximumSizeExceededException(String tag, String exceededType) {
            super(tag,
                    String.format(sizeExceededFmt, exceededType),
                    false /* use BAD not NO */, false /* don't prefix parse error: */);
        }
    }
}
