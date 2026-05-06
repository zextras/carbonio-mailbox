// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.milter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.zimbra.common.account.Key;
import com.zimbra.common.mime.InternetAddress;
import com.zimbra.common.mime.MimeAddressHeader;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.Rights.User;
import com.zimbra.cs.server.ProtocolHandler;

public final class TcpMilterHandler extends ProtocolHandler {

    private enum Context {
        HOSTNAME, ADDRESS, PORT, PROTOFAMILY, SENDER, RECIPIENT
    }

    private static final String MACRO_MAIL_ADDR = "{mail_addr}";
    private static final String MACRO_RCPT_ADDR = "{rcpt_addr}";
    private static final String TO_HEADER = "to";
    private static final String CC_HEADER = "cc";

    private static final int SMFIP_NOHELO = 0x02;
    private static final int SMFIP_NOMAIL = 0x04;
    private static final int SMFIP_NOBODY = 0x10;
    private static final int SMFIP_NOEOH  = 0x40;

    private static final int SMFIF_ADDHDRS = 0x01;
    private static final int SMFIF_CHGHDRS = 0x10;

    private static final byte SMFIR_ACCEPT   = 'a';
    private static final byte SMFIR_CONTINUE = 'c';
    private static final byte SMFIR_CHGHEADER = 'm';
    private static final byte SMFIR_TEMPFAIL = 't';
    private static final byte SMFIR_REPLYCODE = 'y';
    private static final byte SMFIC_OPTNEG   = 'O';

    private static final Charset CHARSET = Charsets.US_ASCII;

    private final Map<Context, String> context = new EnumMap<>(Context.class);
    private final Set<Group> lists = Sets.newHashSetWithExpectedSize(0);
    private final Set<String> visibleAddresses = Sets.newHashSetWithExpectedSize(0);
    private final Provisioning prov;
    private final AccessManager accessMgr;
    private final TcpMilterServer server;

    private DataInputStream in;
    private DataOutputStream out;
    private boolean quit;

    public TcpMilterHandler(TcpMilterServer server) {
        super(server);
        this.server = server;
        this.prov = Provisioning.getInstance();
        this.accessMgr = AccessManager.getInstance();
    }

    @Override
    protected boolean setupConnection(Socket connection) throws IOException {
        int idleTimeout = server.getConfigMaxIdleMilliSeconds();
        if (idleTimeout > 0) {
            connection.setSoTimeout(idleTimeout);
        }
        in = new DataInputStream(connection.getInputStream());
        out = new DataOutputStream(connection.getOutputStream());
        ZimbraLog.milter.info("Connection opened from %s", connection.getRemoteSocketAddress());
        context.clear();
        lists.clear();
        visibleAddresses.clear();
        return true;
    }

    @Override
    protected boolean authenticate() throws IOException {
        return true; // Milter has no authentication phase
    }

    @Override
    protected boolean processCommand() throws Exception {
        MilterPacket packet = readPacket();
        if (packet == null) {
            return false; // EOF
        }
        try {
            processCommand(packet);
        } catch (ServiceException e) {
            ZimbraLog.milter.error("Dropping connection due to server error: %s", e.getMessage(), e);
            return false;
        }
        return !quit;
    }

    private MilterPacket readPacket() throws IOException {
        int len;
        try {
            len = in.readInt();
        } catch (java.io.EOFException e) {
            return null; // clean EOF
        }
        byte cmd = in.readByte();
        byte[] data = null;
        if (len > 1) {
            data = new byte[len - 1];
            in.readFully(data);
        }
        return new MilterPacket(len, cmd, data);
    }

    private void sendPacket(MilterPacket packet) throws IOException {
        out.writeInt(packet.getLength());
        out.write(packet.getCommand());
        byte[] data = packet.getData();
        if (data != null && data.length > 0) {
            out.write(data);
        }
        out.flush();
    }

    @Override
    protected void dropConnection() {
        quit = true;
    }

    @Override
    protected void notifyIdleConnection() {
        ZimbraLog.milter.info("Dropping connection because inactive for more than %s seconds (milter_max_idle_time)",
                server.getConfig().getMaxIdleTime());
    }

    private void clear() {
        context.clear();
        lists.clear();
        visibleAddresses.clear();
    }

    private void processCommand(MilterPacket command) throws IOException, ServiceException {
        switch ((char) command.getCommand()) {
            case 'O': SMFIC_OptNeg(); break;
            case 'D': SMFIC_Macro(command); break;
            case 'C': SMFIC_Connect(command); break;
            case 'M': SMFIC_Mail(); break;
            case 'R': SMFIC_Rcpt(); break;
            case 'L': SMFIC_Header(command); break;
            case 'E': SMFIC_BodyEOB(); break;
            case 'A': SMFIC_Abort(); break;
            case 'Q': SMFIC_Quit(); break;
            default:
                ZimbraLog.milter.debug("Unimplemented command, sending SMFIR_CONTINUE: %s", command.toString());
                sendPacket(new MilterPacket(SMFIR_CONTINUE));
                break;
        }
    }

    private ByteBuffer getDataBuffer(MilterPacket command) {
        byte[] data = command.getData();
        if (data != null && data.length > 0) {
            return ByteBuffer.wrap(data);
        }
        return null;
    }

    private String normalizeAddr(String a) {
        String addr = a.toLowerCase();
        int lb = addr.indexOf('<');
        int rb = addr.indexOf('>');
        return lb >= 0 && rb > lb ? addr.substring(lb + 1, rb) : addr;
    }

    /** Read a null-terminated ASCII string from a ByteBuffer. */
    private static String readNullTerminatedString(ByteBuffer buf) throws IOException {
        int start = buf.position();
        while (buf.hasRemaining() && buf.get(buf.position()) != 0) {
            buf.get();
        }
        int end = buf.position();
        byte[] bytes = new byte[end - start];
        // reset position to start and re-read
        buf.position(start);
        buf.get(bytes);
        if (buf.hasRemaining()) {
            buf.get(); // consume the null terminator
        }
        try {
            return new String(bytes, CHARSET);
        } catch (Exception e) {
            throw new MalformedInputException(bytes.length);
        }
    }

    static Map<String, String> parseMacros(ByteBuffer buf) throws IOException {
        Map<String, String> macros = new HashMap<>();
        try {
            while (buf.hasRemaining()) {
                String key = readNullTerminatedString(buf);
                if (buf.hasRemaining()) {
                    String value = readNullTerminatedString(buf);
                    if (key != null && value != null) {
                        macros.put(key, value);
                    }
                }
            }
        } catch (MalformedInputException e) {
            ZimbraLog.milter.warn("Found non-ascii characters while parsing macros.", e);
        }
        return macros;
    }

    private void getAddrFromMacro(ByteBuffer macroData, String macro, Context attr) throws IOException {
        Map<String, String> macros = parseMacros(macroData);
        String addr = macros.get(macro);
        if (addr != null) {
            String value = normalizeAddr(addr);
            context.put(attr, value);
            ZimbraLog.milter.debug("For macro '%s' %s=%s", macro, attr, value);
        }
    }

    static MimeAddressHeader getToCcAddressHeader(byte[] bytes) {
        MimeAddressHeader mHeader = null;
        try {
            int i = 0;
            String key = null;
            if (bytes.length > 0) {
                while (i < bytes.length && bytes[i] != 0x00) {
                    i++;
                }
            }
            key = new String(Arrays.copyOfRange(bytes, 0, i));
            if (!StringUtil.isNullOrEmpty(key) &&
                    (key.equalsIgnoreCase(TO_HEADER) || key.equalsIgnoreCase(CC_HEADER))) {
                if (bytes.length > i + 1) {
                    byte[] values = Arrays.copyOfRange(bytes, i + 1, bytes.length);
                    mHeader = new MimeAddressHeader(key, values);
                }
            }
        } catch (Exception e) {
            ZimbraLog.milter.warn("Error parsing header.", e);
        }
        return mHeader;
    }

    private void getAddrListsFromHeaders(MilterPacket command) {
        MimeAddressHeader mHeader = getToCcAddressHeader(command.getData());
        if (mHeader != null) {
            for (InternetAddress address : mHeader.getAddresses()) {
                if (address.getAddress() != null) {
                    visibleAddresses.add(address.getAddress().toLowerCase());
                    ZimbraLog.milter.debug("Visible value %s", address.getAddress());
                }
            }
        }
    }

    private void SMFIR_ReplyCode(String code, String reason) throws IOException {
        int len = 1 + 3 + 1 + reason.length() + 1;
        String dataStr = code + " " + reason;
        byte[] data = new byte[len - 1];
        int dataStrLen = dataStr.length();
        for (int i = 0; i < dataStrLen; i++) {
            data[i] = (byte) (dataStr.charAt(i));
        }
        data[dataStrLen] = 0;
        sendPacket(new MilterPacket(len, SMFIR_REPLYCODE, data));
    }

    private void SMFIR_ChgHeader(int index, String name, String value) throws IOException {
        ZimbraLog.milter.info("Add %s: %s", name, value);
        // sizeof(uint32) + name.length + NUL + value.length + NUL
        ByteBuffer buf = ByteBuffer.allocate(6 + name.length() + value.length());
        buf.putInt(index);
        byte[] nameBytes = name.getBytes(CHARSET);
        buf.put(nameBytes);
        buf.put((byte) 0); // NUL terminator
        byte[] valueBytes = value.getBytes(CHARSET);
        buf.put(valueBytes);
        buf.put((byte) 0); // NUL terminator
        sendPacket(new MilterPacket(buf.position() + 1, SMFIR_CHGHEADER, buf.array()));
    }

    private void SMFIC_Connect(MilterPacket command) throws IOException {
        ZimbraLog.milter.debug("SMFIC_Connect");
        ByteBuffer data = getDataBuffer(command);
        if (data != null) {
            context.put(Context.HOSTNAME, readNullTerminatedString(data));
            context.put(Context.PROTOFAMILY, new String(new byte[]{data.get()}, CHARSET));
            context.put(Context.PORT, String.valueOf(data.getShort() & 0xFFFF)); // unsigned short
            context.put(Context.ADDRESS, readNullTerminatedString(data));
            ZimbraLog.milter.info("Connection Info %s", context);
        }
        sendPacket(new MilterPacket(SMFIR_CONTINUE));
    }

    private void SMFIC_Mail() throws IOException {
        ZimbraLog.milter.debug("SMFIC_Mail");
        sendPacket(new MilterPacket(SMFIR_CONTINUE));
    }

    private void SMFIC_Rcpt() throws IOException, ServiceException {
        ZimbraLog.milter.debug("SMFIC_Rcpt");
        String sender = context.get(Context.SENDER);
        if (sender == null) {
            ZimbraLog.milter.warn("Empty sender");
        }
        String rcpt = context.get(Context.RECIPIENT);
        if (rcpt == null) {
            ZimbraLog.milter.warn("Empty recipient");
        }
        if (sender == null || rcpt == null) {
            sendPacket(new MilterPacket(SMFIR_TEMPFAIL));
            return;
        }
        if (prov.isDistributionList(rcpt)) {
            Group group = prov.getGroupBasic(Key.DistributionListBy.name, rcpt);
            if (group != null) {
                if (!accessMgr.canDo(sender, group, User.R_sendToDistList, false)) {
                    ZimbraLog.milter.debug("Sender is not allowed to email this distribution list: %s", rcpt);
                    SMFIR_ReplyCode("571", "571 Sender is not allowed to email this distribution list: " + rcpt);
                    return;
                }
                lists.add(group);
                ZimbraLog.milter.debug("group %s has been added into the list.", group);
            } else {
                ZimbraLog.milter.debug("rcpt %s is a list but not a group?", rcpt);
            }
        } else {
            ZimbraLog.milter.debug("%s is not a distribution list.", rcpt);
        }
        sendPacket(new MilterPacket(SMFIR_CONTINUE));
    }

    private void SMFIC_Abort() {
        ZimbraLog.milter.info("SMFIC_Abort session reset");
        clear();
    }

    private void SMFIC_Macro(MilterPacket command) throws IOException {
        ZimbraLog.milter.debug("SMFIC_Macro");
        ByteBuffer data = getDataBuffer(command);
        if (data != null) {
            byte cmd = data.get();
            if ((char) cmd == 'M') {
                getAddrFromMacro(data, MACRO_MAIL_ADDR, Context.SENDER);
            } else if ((char) cmd == 'R') {
                getAddrFromMacro(data, MACRO_RCPT_ADDR, Context.RECIPIENT);
            }
        }
    }

    private void SMFIC_OptNeg() throws IOException {
        ZimbraLog.milter.debug("SMFIC_OptNeg");
        ByteBuffer data = ByteBuffer.allocate(12);
        data.putInt(2); // version
        data.putInt(SMFIF_ADDHDRS | SMFIF_CHGHDRS); // actions
        data.putInt(SMFIP_NOHELO | SMFIP_NOMAIL | SMFIP_NOEOH | SMFIP_NOBODY); // protocol
        sendPacket(new MilterPacket(13, SMFIC_OPTNEG, data.array()));
    }

    private void SMFIC_Header(MilterPacket command) throws IOException {
        ZimbraLog.milter.debug("SMFIC_Header");
        getAddrListsFromHeaders(command);
        sendPacket(new MilterPacket(SMFIR_CONTINUE));
    }

    private void SMFIC_BodyEOB() throws IOException {
        ZimbraLog.milter.debug("SMFIC_BodyEOB");
        Set<String> listAddrs = Sets.newHashSetWithExpectedSize(lists.size());
        Set<String> replyToAddrs = Sets.newHashSetWithExpectedSize(lists.size());
        for (Group group : lists) {
            if (group == null) {
                ZimbraLog.milter.warn("null group in group list!?!");
                continue;
            }
            if (visibleAddresses.contains(group.getMail().toLowerCase())) {
                listAddrs.add(group.getMail());
                if (group.isPrefReplyToEnabled()) {
                    String addr = group.getPrefReplyToAddress();
                    if (Strings.isNullOrEmpty(addr)) {
                        addr = group.getMail();
                    }
                    String disp = group.getPrefReplyToDisplay();
                    if (Strings.isNullOrEmpty(disp)) {
                        disp = group.getDisplayName();
                    }
                    replyToAddrs.add(new InternetAddress(disp, addr).toString());
                }
            }
        }
        if (!listAddrs.isEmpty()) {
            SMFIR_ChgHeader(1, "X-Zimbra-DL", Joiner.on(", ").join(listAddrs));
        }
        if (!replyToAddrs.isEmpty()) {
            SMFIR_ChgHeader(1, "Reply-To", Joiner.on(", ").join(replyToAddrs));
        }
        sendPacket(new MilterPacket(SMFIR_ACCEPT));
    }

    private void SMFIC_Quit() {
        ZimbraLog.milter.info("SMFIC_Quit");
        quit = true;
    }
}
