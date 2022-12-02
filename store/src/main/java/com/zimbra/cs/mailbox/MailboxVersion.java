// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.Iterator;

import com.google.common.base.Splitter;
import com.zimbra.common.service.ServiceException;

public final class MailboxVersion {
    // These should be incremented with changes to serialization format.
    private static final short CURRENT_MAJOR = 2; // range: 0 - Short.MAX_VALUE
    private static final short CURRENT_MINOR = 7; // range: 0 - Short.MAX_VALUE

    private final short majorVer;
    private final short minorVer;

    // deprecated ZIMBRA.MAILBOX_METADATA config key for storing version as encoded metadata
    static final String MD_CONFIG_VERSION = "ver";

    static final MailboxVersion CURRENT = new MailboxVersion();

    public static MailboxVersion getCurrent() {
        return CURRENT;
    }

    MailboxVersion() {
        majorVer = CURRENT_MAJOR;
        minorVer = CURRENT_MINOR;
    }

    public MailboxVersion(short major, short minor) {
        majorVer = major;
        minorVer = minor;
    }

    MailboxVersion(MailboxVersion other) {
        majorVer = other.majorVer;
        minorVer = other.minorVer;
    }

    public short getMajor() {
        return majorVer;
    }

    public short getMinor() {
        return minorVer;
    }

    static MailboxVersion fromMetadata(Metadata md) throws ServiceException {
        // unknown version are set to 1.0
        short major = 1;
        short minor = 0;

        if (md != null) {
            major = (short) md.getLong("vmaj", 1);
            minor = (short) md.getLong("vmin", 0);
        }

        return new MailboxVersion(major, minor);
    }

    private static final Splitter SPLITTER = Splitter.on('.').trimResults();

    public static MailboxVersion parse(String s) {
        if (s == null) {
            return null;
        }

        Iterator<String> components = SPLITTER.split(s).iterator();
        try {
            short major = 1;
            short minor = 0;

            if (components.hasNext()) {
                major = Short.parseShort(components.next());
            }
            if (components.hasNext()) {
                minor = Short.parseShort(components.next());
            }

            MailboxVersion version = new MailboxVersion(major, minor);
            return version.isLatest() ? CURRENT : version;
        } catch (NumberFormatException nfe) {
            return new MailboxVersion((short) 1, (short) 0);
        }
    }

    /** Returns if this version is at least as high as the version specified by
     *  major and minor.
     *
     * @return true if this version is higher than or equal to major/minor,
     *         false if this version is lower */
    public boolean atLeast(int major, int minor) {
        return majorVer > major || (majorVer == major && minorVer >= minor);
    }

    /** Returns if this version is at least as high as version b.
     *
     * @return true if this version is higher than or equal to version b, false
     *         if this version is lower than version b */
    public boolean atLeast(MailboxVersion b) {
        return atLeast(b.majorVer, b.minorVer);
    }

    public boolean isLatest() {
        return majorVer == CURRENT_MAJOR && minorVer == CURRENT_MINOR;
    }

    /** Returns if this version is higher than latest known code version. */
    public boolean tooHigh() {
        return majorVer > CURRENT_MAJOR || (majorVer == CURRENT_MAJOR && minorVer > CURRENT_MINOR);
    }

    @Override
    public String toString() {
        return majorVer + "." + minorVer;
    }
}
