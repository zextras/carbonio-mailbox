// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.IOException;
import java.io.OutputStream;

import com.zimbra.common.util.tar.TarEntry;
import com.zimbra.common.util.tar.TarOutputStream;
import com.zimbra.cs.service.formatter.ArchiveFormatter.ArchiveOutputEntry;
import com.zimbra.cs.service.formatter.ArchiveFormatter.ArchiveOutputStream;

public class TarArchiveOutputStream implements ArchiveOutputStream {
    public class TarArchiveOutputEntry implements ArchiveOutputEntry {
        private TarEntry entry;

        public TarArchiveOutputEntry(String path, String name, int type, long
            date) {
            entry = new TarEntry(path);
            entry.setGroupName(name);
            entry.setMajorDeviceId(type);
            entry.setModTime(date);
        }

        @Override
        public void setUnread() { entry.setMode(entry.getMode() & ~0200); }
        @Override
        public void setSize(long size) { entry.setSize(size); }
    }

    private TarOutputStream os;

    public TarArchiveOutputStream(OutputStream os, String cset) throws
        IOException {
        this.os = new TarOutputStream(os, cset);
        this.os.setLongFileMode(TarOutputStream.LONGFILE_GNU);
    }
    @Override
    public void close() throws IOException { os.close(); }
    @Override
    public void closeEntry() throws IOException { os.closeEntry(); }
    @Override
    public OutputStream getOutputStream() { return os; }
    @Override
    public int getRecordSize() { return os.getRecordSize(); }
    @Override
    public ArchiveOutputEntry newOutputEntry(String path, String name,
        int type, long date) {
        return new TarArchiveOutputEntry(path, name, type, date);
    }
    @Override
    public void putNextEntry(ArchiveOutputEntry entry) throws IOException {
        os.putNextEntry(((TarArchiveOutputEntry)entry).entry);
    }
    @Override
    public void write(byte[] buf) throws IOException { os.write(buf); }
    @Override
    public void write(byte[] buf, int offset, int len) throws IOException {
        os.write(buf, offset, len);
    }
}
