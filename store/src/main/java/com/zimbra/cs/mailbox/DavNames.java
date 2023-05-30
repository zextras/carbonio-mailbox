// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zimbra.common.util.ZimbraLog;

/**
 * Utility class to handle EXCEPTIONS to the conventional DAV name scheme.
 * This is typically needed when a client chooses the name for an item on creation via the PUT method and that
 * name doesn't follow our preferred convention.
 * TODO: Make these names persistent rather than in memory.
 */
public class DavNames {
    public static final class DavName {
        public final int mailbox_id;
        public final int folder_id;
        public final String davBaseName;
        private DavName(int mailbox_id, int folder_id, String davBaseName) {
            this.mailbox_id = mailbox_id;
            this.folder_id = folder_id;
            this.davBaseName = davBaseName;
        }
        public static DavName create(int mailbox_id, int folder_id, String davBaseName) {
            return new DavName(mailbox_id, folder_id, davBaseName);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("DavName:mbox=").append(mailbox_id)
                    .append(" folder=").append(folder_id).append(" name=").append(davBaseName);
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DavName other = (DavName) obj;
            return toString().equals(other.toString());
        }
    }

    public static final class MboxAndId {
        public final int mailbox_id;
        public final int item_id;

        private MboxAndId(int mailbox_id, int item_id) {
            this.mailbox_id = mailbox_id;
            this.item_id = item_id;
        }

        public static MboxAndId create(int mailbox_id, int item_id) {
            return new MboxAndId(mailbox_id, item_id);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("MboxAndId:mbox=").append(mailbox_id).append(" item=").append(item_id);
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MboxAndId other = (MboxAndId) obj;
            return toString().equals(other.toString());
        }
    }

    private static final Map<DavName, Integer> nameToID = new ConcurrentHashMap<DavName, Integer>();
    private static final Map<MboxAndId, DavName> idToName = new ConcurrentHashMap<MboxAndId, DavName>();

    private DavNames() {
    }

    public static Integer get(DavName davName) {
        Integer ret = nameToID.get(davName);
        ZimbraLog.dav.debug("DavNameMap.get '%s' --> id=%d", davName, ret);
        return ret;
    }

    public static DavName get(MboxAndId mboxAndId) {
        DavName ret = idToName.get(mboxAndId);
        ZimbraLog.dav.debug("DavNameMap.get %s ---> '%s'", mboxAndId, ret);
        return ret;

    }

    public static DavName get(int mailbox_id, int item_id) {
        return get(MboxAndId.create(mailbox_id, item_id));
    }

    public static Integer get(int mailbox_id, int folder_id, String davBaseName) {
        return get(DavName.create(mailbox_id, folder_id, davBaseName));
    }

    public static synchronized Integer put(DavName davName, int id) {
        ZimbraLog.dav.debug("DavNameMap.put '%s' id=%d", davName, id);
        idToName.put(MboxAndId.create(davName.mailbox_id, id), davName);
        return nameToID.put(davName, id);
    }

    public static synchronized void remove(DavName davName) {
        ZimbraLog.dav.debug("DavNameMap.remove '%s'", davName);
        Integer id = nameToID.get(davName);
        idToName.remove(davName);
        if (id != null) {
            nameToID.remove(MboxAndId.create(davName.mailbox_id, id));
        }
    }

    public static synchronized void remove(int mailbox_id, int item_id) {
        MboxAndId mboxAndId = MboxAndId.create(mailbox_id, item_id);
        ZimbraLog.dav.debug("DavNameMap.remove '%s'", mboxAndId);
        DavName davName = idToName.get(mboxAndId);
        if (davName != null) {
            idToName.remove(davName);
        }
        idToName.remove(mboxAndId);
    }
}
