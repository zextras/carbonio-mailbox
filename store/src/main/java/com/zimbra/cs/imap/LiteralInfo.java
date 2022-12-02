// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import org.apache.mina.filter.codec.ProtocolDecoderException;

public class LiteralInfo {
    int count;
    boolean blocking;

    public static LiteralInfo parse(String line) throws ProtocolDecoderException {
        if (line.endsWith("}")) {
            int i = line.lastIndexOf('{');
            if (i >= 0) {
                LiteralInfo li = new LiteralInfo();
                String s = line.substring(i + 1, line.length() - 1);
                if (s.endsWith("+")) {
                    s = s.substring(0, s.length() - 1);
                } else {
                    li.blocking = true;
                }
                li.count = parseCount(s);
                if (li.count < 0) {
                    throw new NioImapDecoder.TooBigLiteralException(line);
                }
                return li;
            }
        }
        return null;
    }

    public int getCount() { return count; }
    public boolean isBlocking() { return blocking; }
    
    private static int parseCount(String s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            int d = Character.digit(s.charAt(i), 10);
            if (d == -1) return -1;
            n = n * 10 + d;
            if (n < 0) return -1; // Overflow
        }
        return n;
    }
}
