// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.zimbra.common.calendar.ICalTimeZone;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.calendar.Util;
import com.zimbra.cs.mailbox.calendar.tzfixup.TimeZoneFixupRules;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class FixCalendarItemTZ extends RedoableOp {

    private int mId;
    private Map<String, ICalTimeZone> mReplacementMap;

    public FixCalendarItemTZ() {
        super(MailboxOperation.FixCalendarItemTZ);
    }

    public FixCalendarItemTZ(int mailboxId, int itemId) {
        this();
        setMailboxId(mailboxId);
        mId = itemId;
    }

    public void setReplacementMap(Map<String, ICalTimeZone> replacementMap) {
        mReplacementMap = replacementMap;
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mId);
        if (mReplacementMap != null) {
            out.writeInt(mReplacementMap.size());
            for (Entry<String, ICalTimeZone> entry : mReplacementMap.entrySet()) {
                String tzid = entry.getKey();
                ICalTimeZone newTZ = entry.getValue();
                String newTZMeta = null;
                if (newTZ != null)
                    newTZMeta = Util.encodeAsMetadata(newTZ).toString();
                out.writeUTF(tzid);
                out.writeUTF(newTZMeta);
            }
        } else {
            out.writeInt(0);  // map size == 0
        }
    }

    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        mId = in.readInt();
        int numReplacements = in.readInt();
        if (numReplacements > 0) {
            mReplacementMap = new HashMap<>(numReplacements);
            for (int i = 0; i < numReplacements; i++) {
                String tzid = in.readUTF();
                String newTZMeta = in.readUTF();
                try {
                    ICalTimeZone newTZ = null;
                    if (newTZMeta != null)
                        newTZ = Util.decodeTimeZoneFromMetadata(new Metadata(newTZMeta));
                    mReplacementMap.put(tzid, newTZ);
                } catch (ServiceException e) {
                    IOException ioe = new IOException("Error deserializing timezone");
                    ioe.initCause(e);
                    throw ioe;
                }
            }
        }
    }

    @Override
    protected String getPrintableData() {
        StringBuilder sb = new StringBuilder("id=");
        sb.append(mId);
        if (mReplacementMap != null) {
            sb.append(", replacementMap=[");
            for (Entry<String, ICalTimeZone> entry : mReplacementMap.entrySet()) {
                String tzid = entry.getKey();
                ICalTimeZone newTZ = entry.getValue();
                sb.append("\n");
                sb.append("oldTZID=\"").append(tzid).append("\"\n==> newTZ: ").append(newTZ.toString()).append(",");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    @Override
    public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        TimeZoneFixupRules rules = new TimeZoneFixupRules(mReplacementMap);
        mbox.fixCalendarItemTZ(getOperationContext(), mId, rules);
    }
}
