// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.zip.ZipOutputStream;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.UserServletException;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;

public class ZipFormatter extends ArchiveFormatter {
    public static class ZipArchiveInputStream implements ArchiveInputStream {
        public static class ZipArchiveInputEntry implements ArchiveInputEntry {
            private ZipEntry entry;

            public ZipArchiveInputEntry(ZipInputStream is) throws IOException {
                entry = is.getNextEntry();
            }
            public long getModTime() { return entry.getTime(); }
            public String getName() { return entry.getName(); }
            public long getSize() { return entry.getSize(); }
            public int getType() { return 0; }
            public boolean isUnread() {
                return entry.getComment() != null &&
                    entry.getComment().endsWith("-unread");
            }
        }
        
        private ZipInputStream is;
        
        public ZipArchiveInputStream(InputStream is, String cset) {
            this.is = new ZipInputStream(is, Charset.forName(cset));
        }
        
        public void close() throws IOException { is.close(); }
        public InputStream getInputStream() { return is; }
        public ArchiveInputEntry getNextEntry() throws IOException {
            ZipArchiveInputEntry zaie = new ZipArchiveInputEntry(is);
            
            return zaie.entry == null ? null : zaie;
        }
        public int read(byte[] buf, int offset, int len) throws IOException {
            return is.read(buf, offset, len);
        }
    }
    
    public static class ZipArchiveOutputStream implements ArchiveOutputStream {
        public static class ZipArchiveOutputEntry implements ArchiveOutputEntry {
            private com.zimbra.common.util.zip.ZipEntry entry;

            public ZipArchiveOutputEntry(String path, String name, int type, long
                date) {
                entry = new com.zimbra.common.util.zip.ZipEntry(path);
                entry.setComment(name);
                entry.setTime(date);
                entry.setUnixMode(0660);
            }
            public void setUnread() {
                entry.setUnixMode(0640);
                entry.setComment(entry.getComment() + "-unread");
            }
            public void setSize(long size) { entry.setSize(size); }
        }
        
        private ZipOutputStream os;
        
        public ZipArchiveOutputStream(OutputStream os, String cset, int lvl)
            throws IOException {
            this.os = new ZipOutputStream(os);
            this.os.setEncoding(cset);
            if (lvl >= 0 && lvl <= 9)
                this.os.setLevel(lvl);
        }
        public void close() throws IOException { os.close(); }
        public void closeEntry() throws IOException { os.closeEntry(); }
        public OutputStream getOutputStream() { return os; }
        public int getRecordSize() { return 2048; }
        public ArchiveOutputEntry newOutputEntry(String path, String name,
            int type, long date) {
            return new ZipArchiveOutputEntry(path, name, type, date);
        }
        public void putNextEntry(ArchiveOutputEntry entry) throws IOException {
            os.putNextEntry(((ZipArchiveOutputEntry)entry).entry);
        }
        public void write(byte[] buf) throws IOException { os.write(buf); }
        public void write(byte[] buf, int offset, int len) throws IOException {
            os.write(buf, offset, len);
        }
    }

    @Override public String[] getDefaultMimeTypes() {
        return new String[] { "application/zip", "application/x-zip-compressed" };
    }

    @Override 
    public FormatType getType() { 
        return FormatType.ZIP; 
    }

    @Override protected boolean getDefaultMeta() { return false; }
    
    protected ArchiveInputStream getInputStream(UserServletContext context,
        String charset) throws IOException, ServiceException, UserServletException {
        return new ZipArchiveInputStream(context.getRequestInputStream(-1),
            charset);
    }

    protected ArchiveOutputStream getOutputStream(UserServletContext context, String
        charset) throws IOException {
        OutputStream os = context.resp.getOutputStream();
        String zlv = context.params.get(UserServlet.QP_ZLV);
        int lvl = -1;
        
        if (zlv != null && zlv.length() > 0) {
            try {
                lvl = Integer.parseInt(zlv);
            } catch (NumberFormatException x) {}
        }
        return new ZipArchiveOutputStream(os, charset, lvl);
    }
}
