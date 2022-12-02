// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import com.zimbra.cs.mailclient.CommandFailedException;
import com.zimbra.cs.mailclient.MailException;
import com.zimbra.cs.mailclient.ParseException;
import com.zimbra.cs.mailclient.util.DateUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Date;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;

public class ImapRequest {
    private final ImapConnection connection;
    private final String tag;
    private final Atom cmd;
    private List<Object> params;
    private ResponseHandler responseHandler;
    private DataHandler dataHandler;

    public ImapRequest(ImapConnection connection, Atom cmd) {
        this.connection = connection;
        this.tag = connection.newTag();
        this.cmd = cmd;
    }

    public ImapRequest(ImapConnection connection, Atom cmd, Object... params) {
        this(connection, cmd);
        for (Object param : params) {
            addParam(param);
        }
    }

    public void addParam(Object param) {
        if (param == null) {
            throw new NullPointerException();
        }
        if (params == null) {
            params = new ArrayList<Object>();
        }
        params.add(param);
    }

    public void setResponseHandler(ResponseHandler handler) {
        this.responseHandler = handler;
    }

    public void setDataHandler(DataHandler handler) {
        this.dataHandler = handler;
    }

    public String getTag() { return tag; }
    public Atom getCommand() { return cmd; }
    public List<Object> getParams() { return params; }
    public ResponseHandler getResponseHandler() { return responseHandler; }
    public DataHandler getDataHandler() { return dataHandler; }

    public boolean isAuthenticate() {
        return CAtom.AUTHENTICATE.atom().equals(cmd);
    }

    public boolean isIdle() {
        return CAtom.IDLE.atom().equals(cmd);
    }

    public boolean isSelectOrExamine() {
        switch (cmd.getCAtom()) {
        case SELECT: case EXAMINE:
            return true;
        default:
            return false;
        }
    }

    public ImapResponse send() throws IOException {
        try {
            return connection.sendRequest(this);
        } catch (SocketTimeoutException e) {
            connection.close();
            throw failed("Timeout waiting for response", e);
        } catch (MailException e) {
            if (e instanceof ParseException) {
                connection.close();
            }
            throw failed("Error in response", e);
        } catch (IOException e) {
            connection.close();
            throw e;
        }
    }

    public ImapResponse sendCheckStatus() throws IOException {
        ImapResponse res = send();
        checkStatus(res);
        return res;
    }

    public void checkStatus(ImapResponse res) throws IOException {
        if (!res.isTagged()) {
             throw new MailException("Expected a tagged response");
         }
         if (!tag.equalsIgnoreCase(res.getTag())) {
             throw new MailException(
                 "Unexpected tag in response(expected " + tag + " but got " +
                 res.getTag() + ")");
         }
         if (!res.isOK()) {
             throw failed(res.getResponseText().getText());
         }
    }

    public void write(ImapOutputStream out) throws IOException {
        out.write(tag);
        out.write(' ');
        out.write(cmd.getName());
        if (params != null && params.size() > 0) {
            out.write(' ');
            if (cmd.getCAtom() == CAtom.LOGIN && params.size() > 1) {
                writeData(out, params.get(0));
                out.write(' ');
                writeUntracedList(out, params.subList(1, params.size()));
            } else {
                writeList(out, params);
            }
        }
        out.newLine();
        out.flush();
        out.trace();
    }

    private void writeUntracedList(ImapOutputStream out, List<Object> data) throws IOException {
        out.setPrivacy(true);
        writeList(out, data);
        out.setPrivacy(false);
    }

    private void writeData(ImapOutputStream out, Object data)
        throws IOException {
        if (data instanceof String) {
            String s = (String) data;
            out.write(s.length() > 0 ? s : "\"\"");
        } else if (data instanceof Atom) {
            ((Atom) data).write(out);
        } else if (data instanceof Quoted) {
            ((Quoted) data).write(out);
        } else if (data instanceof Literal) {
            connection.writeLiteral((Literal) data);
        } else if (data instanceof Flags) {
            ((Flags) data).write(out);
        } else if (data instanceof MailboxName) {
            String encoded = ((MailboxName) data).encode();
            writeData(out, ImapData.asAString(encoded));
        } else if (data instanceof IDInfo) {
            writeData(out, ((IDInfo) data).toRequestParam());
        } else if (data instanceof Date) {
            writeData(out, new Quoted(toInternalDate((Date) data)));
        } else if (data instanceof Object[]) {
            writeData(out, Arrays.asList((Object[]) data));
        } else if (data instanceof List) {
            out.write('(');
            writeList(out, (List<?>) data);
            out.write(')');
        } else if (data instanceof AppendMessage) {
            writeList(out, ((AppendMessage) data).getData());
        } else {
            writeData(out, data.toString());
        }
    }

    // Format is dd-MMM-yyyy HH:mm:ss Z
    private static String toInternalDate(Date date) {
        return DateUtil.toImapDateTime(date);
    }

    private void writeList(ImapOutputStream out, List<?> list)
        throws IOException {
        Iterator<?> it = list.iterator();
        if (it.hasNext()) {
            writeData(out, it.next());
            while (it.hasNext()) {
                out.write(' ');
                writeData(out, it.next());
            }
        }
    }

    public CommandFailedException failed(String error) {
        return failed(error, null);
    }

    public CommandFailedException failed(String error, Throwable cause) {
        CommandFailedException cfe = new CommandFailedException(cmd.getName(), error);
        try {
            cfe.setRequest(toString());
        } catch (Exception e) {
        }
        cfe.initCause(cause);
        return cfe;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tag).append(' ').append(cmd);
        if (params != null) {
            if (cmd.getCAtom() == CAtom.LOGIN && params.size() > 1) {
                sb.append(' ');
                append(sb, params.get(0));
                sb.append(" <password>");
            } else {
                for (Object param : params) {
                    sb.append(' ');
                    append(sb, param);
                }
            }
        }
        return sb.toString();
    }

    private void append(StringBuilder sb, Object param) {
        if (param instanceof String) {
            String s = (String) param;
            sb.append(s.length() > 0 ? s : "\"\"");
        } else if (param instanceof Quoted) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ((Quoted) param).write(baos);
            } catch (IOException e) {
                throw new AssertionError();
            }
            sb.append(baos.toString());
        } else if (param instanceof Literal) {
            sb.append("<literal ");
            sb.append(((Literal) param).getSize());
            sb.append(" bytes>");
        } else if (param instanceof MailboxName) {
            String encoded = ((MailboxName) param).encode();
            append(sb, ImapData.asAString(encoded));
        } else if (param instanceof IDInfo) {
            append(sb, ((IDInfo) param).toRequestParam());
        } else if (param instanceof Date) {
            append(sb, new Quoted(toInternalDate((Date) param)));
        } else if (param instanceof Object[]) {
            append(sb, Arrays.asList((Object[]) param));
        } else if (param instanceof List) {
            sb.append('(');
            Iterator<?> it = ((List<?>) param).iterator();
            if (it.hasNext()) {
                append(sb, it.next());
                while (it.hasNext()) {
                    sb.append(' ');
                    append(sb, it.next());
                }
            }
            sb.append(')');
        } else { // Atom, Flags, Object
            sb.append(param.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        Date date = new Date();
        System.out.println("new = " + toInternalDate(date));
    }
}
