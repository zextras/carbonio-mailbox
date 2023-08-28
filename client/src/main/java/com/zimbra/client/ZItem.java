// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.google.common.base.Strings;
import com.zimbra.client.event.ZModifyEvent;
import com.zimbra.common.service.ServiceException;

public interface ZItem {
    public static final Flag[] CHAR2FLAG = new Flag[127];
    public enum Flag {
        UNREAD(-10, 'u'),
        FLAGGED(-6, 'f'),
        HIGH_PRIORITY(-11, '!'),
        LOW_PRIORITY(-12, '?'),
        ATTACHED(-2, 'a'),
        REPLIED(-3, 'r'),
        FROM_ME(-1, 's'),
        FORWARDED(-4, 'w'),
        DRAFT(-7, 'd'),
        DELETED(-8, 'x'),
        NOTIFIED(-9, 'n'),
        NOTE(-16, 't');

        final int id;
        final char ch;
        final int bitmask;

        public char getFlagChar() { return ch; }

        private Flag(int id, char ch) {
            this.id = id;
            this.ch = ch;
            this.bitmask = 1 << (-id - 1);
            CHAR2FLAG[ch] = this;
        }

        public static String toNameList(String flags) {
            if (flags == null || flags.length() == 0) return "";
            StringBuilder sb = new StringBuilder();
            for (int i=0; i < flags.length(); i++) {
                String v = null;
                for (Flag f : Flag.values()) {
                    if (f.getFlagChar() == flags.charAt(i)) {
                        v = f.name();
                        break;
                    }
                }
                if (sb.length() > 0) sb.append(", ");
                sb.append(v == null ? flags.substring(i, i+1) : v);
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return Character.toString(ch);
        }

        /**
         * Returns the "external" flag bitmask for the given flag string, which includes {@link Flag#BITMASK_UNREAD}.
         */
        public static int toBitmask(String flags) {
            if (Strings.isNullOrEmpty(flags)) {
                return 0;
            }

            int bitmask = 0;
            for (int i = 0, len = flags.length(); i < len; i++) {
                char c = flags.charAt(i);
                Flag flag = c > 0 && c < 127 ? CHAR2FLAG[c] : null;
                if (flag != null) {
                    bitmask |= flag.bitmask;
                }
            }
            return bitmask;
        }
    }

    public String getId();
    public String getUuid();
}

