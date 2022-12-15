// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.pop3;

import com.zimbra.cs.mailclient.MailInputStream;

import java.io.IOException;
import java.io.EOFException;

/**
 * POP3 server response message.
 */
public class Pop3Response {
    private final String command;
    private String status;
    private String message;
    private ContentInputStream cis;

    private static final String OK = "+OK";
    private static final String ERR = "-ERR";
    private static final String CONTINUATION = "+";

    private static final String CAPA = "CAPA";
    private static final String LIST = "LIST";
    private static final String RETR = "RETR";
    private static final String UIDL = "UIDL";

    public static Pop3Response read(String cmd, MailInputStream is) throws IOException {
        Pop3Response res = new Pop3Response(cmd);
        res.readResponse(is);
        return res;
    }

    private Pop3Response(String cmd) {
        command = cmd;
    }

    private void readResponse(MailInputStream is) throws IOException {
        String line = is.readLine();
        if (line == null) {
            throw new EOFException("Unexpected end of stream");
        }
        is.trace();
        int i = line.indexOf(' ');
        if (i == -1) {
            status = line;
            message = "";
        } else {
            status = line.substring(0, i);
            message = line.substring(i).trim();
        }
        if (isOK() && hasContent(command)) {
            cis = new ContentInputStream(is);
        }
    }

    private static boolean hasContent(String cmd) {
        return LIST.equalsIgnoreCase(cmd) || RETR.equalsIgnoreCase(cmd) ||
               UIDL.equalsIgnoreCase(cmd) || CAPA.equalsIgnoreCase(cmd);
    }

    public boolean isOK() { return status.equals(OK); }
    public boolean isERR() { return status.equals(ERR); }
    public boolean isContinuation() { return status.equals(CONTINUATION); }

    public String getMessage() { return message; }

    public ContentInputStream getContentInputStream() {
        return cis;
    }

    public void dispose() {
        if (cis != null) {
            try {
                cis.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
