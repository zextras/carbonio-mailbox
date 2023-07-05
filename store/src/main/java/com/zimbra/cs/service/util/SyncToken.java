// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;

/**
 * A short identifier which identifies the synchronization state between a client and the server.
 *
 * 1)   "INTEGER"  -- this is the highest change ID the client knows about (see Mailbox.getLastChangeID)
 *
 *    OR
 *
 * 2)   "INTEGER-INTEGER"
 *         -- the first integer is the highest change ID that the client has *all* the data for
 *         -- the second integer is the highest item id in the NEXT CHANGE ID that the client has data for
 *
 *         e.g. "4-32" means "I have all of change 4, AND I have up to item 32 in change 5"
 * 3)   "INTEGER-INTEGER:dINTEGER-INTEGER"
 *         -- the first integer is the highest change ID that the client has *all* the data for
 *         -- the second integer is the highest item id in the NEXT CHANGE ID that the client has data for
 *         -- :d for Deleted Cut off. as there is delete pagination introduced.
 *         :dINTEGER-INTEGER
 *         -- the first integer is the highest delete ID that the client has *all* the data for
 *         -- the second integer is the highest item id in the NEXT DELETE ID that the client has data for
 *         e.g. "4-32:d3-30" means "I have all of change 4, AND I have up to item 32 in change 5 AND
 *              I have all of delete 3, AND I have up to item 30 in delete"
 * Similar format :
 *           a) 4-32:d3-30
 *           b) 4-32:d6
 *           c) 4:d3-30
 *           d) 4-32:d3-30
 */
public class SyncToken implements Cloneable, Comparable {
    private int mChangeId;
    private int mChangeOffsetInNext = -1;
    private int mDeleteModSeq = -1;
    private int mDeleteOffsetInNext = -1;
    private final String MODSEQ_ITEMID_SEPARATOR = "-";
    private final String CHANGE_DEL_SEPARATOR = ":d";

    public SyncToken(int changeid) {
        assert(changeid >= 0);
        mChangeId = changeid;
    }

    public SyncToken(int changeid, int offsetInNextChange) {
        assert(changeid >= 0 && offsetInNextChange >= 0);
        mChangeId = changeid;
        mChangeOffsetInNext = offsetInNextChange;
    }

    public SyncToken(int changeid, int offsetInNextChange, int deleteid, int offsetInNextDelete) {
        mChangeId = changeid;
        mChangeOffsetInNext = offsetInNextChange;
        mDeleteModSeq = deleteid;
        mDeleteOffsetInNext = offsetInNextDelete;
    }

    public SyncToken(String token) throws ServiceException {
        int del2modDelimiter = token.indexOf(CHANGE_DEL_SEPARATOR);
        String modToken = token;
        String delToken = "";
        if (del2modDelimiter > 0) {
            modToken = token.substring(0, del2modDelimiter);
            delToken = token.substring(del2modDelimiter+2);
        }
        try {
            int delimiter = modToken.indexOf(MODSEQ_ITEMID_SEPARATOR);
            if (delimiter < 1) {
                mChangeId = Integer.parseInt(modToken);
                mChangeOffsetInNext = -1;
            } else {
                mChangeId = Integer.parseInt(modToken.substring(0, delimiter));
                mChangeOffsetInNext = Integer.parseInt(modToken.substring(delimiter + 1));
            }
            if (!StringUtil.isNullOrEmpty(delToken)) {
                delimiter = delToken.indexOf(MODSEQ_ITEMID_SEPARATOR);
                if (delimiter < 1) {
                    mDeleteModSeq = Integer.parseInt(delToken);
                    mDeleteOffsetInNext = -1;
                } else {
                    mDeleteModSeq = Integer.parseInt(delToken.substring(0, delimiter));
                    mDeleteOffsetInNext = Integer.parseInt(delToken.substring(delimiter + 1));
                }
            } else {
                mDeleteModSeq = -1;
                mDeleteOffsetInNext = -1;
            }
        } catch (NumberFormatException nfe) {
            throw ServiceException.INVALID_REQUEST("malformed sync token: " + token, nfe);
        }
    }

    public void setChangeModSeq(int changeModSeq) {
        this.mChangeId = changeModSeq;
    }

    public void setChangeItemId(int changeItemId) {
        this.mChangeOffsetInNext = changeItemId;
    }

    public void setDeleteModSeq(int deleteModSeq) {
        this.mDeleteModSeq = deleteModSeq;
    }

    public void setDeleteItemId(int deleteItemId) {
        this.mDeleteOffsetInNext = deleteItemId;
    }

    public int getChangeId() { return mChangeId; }
    public boolean hasOffsetInNext() { return mChangeOffsetInNext > 0; }
    public int getOffsetInNext() { return mChangeOffsetInNext; }
    public int getDeleteModSeq() { return mDeleteModSeq; }
    public boolean hasDeleteOffsetInNext() { return mDeleteOffsetInNext > 0; }
    public int getDeleteOffsetInNext() { return mDeleteOffsetInNext; }

    public String toString() {
        StringBuffer token = new StringBuffer();
        if (this.mChangeId > 0) {
            token.append("").append(this.mChangeId);
        }
        if (this.mChangeOffsetInNext > 0) {
            token.append(MODSEQ_ITEMID_SEPARATOR).append(this.mChangeOffsetInNext);
        }
        if (this.mDeleteModSeq > 0) {
            token.append(CHANGE_DEL_SEPARATOR).append(this.mDeleteModSeq);
            if (this.mDeleteOffsetInNext > 0) {
                token.append(MODSEQ_ITEMID_SEPARATOR).append(this.mDeleteOffsetInNext);
            }
        }
        return token.toString();
    }

    /**
     * TRUE if this syncToken is AFTER or UP-TO-DATE with the passed-in token
     *
     * @param changeId
     * @return
     */
    public boolean after(int changeId) {
        return mChangeId >= changeId;
    }

    public boolean after(int changeId, int offset) {
        if (mChangeId < changeId)
            return false;
        if (mChangeId > changeId)
            return true;
        return (mChangeOffsetInNext >= offset);
    }

    public boolean after(int changeid, int offsetInNextChange, int deleteid, int offsetInNextDelete) {
        SyncToken other = new SyncToken(changeid, offsetInNextChange, deleteid, offsetInNextDelete);
        return this.after(other);
    }

    public boolean after(SyncToken other) {
        if (this.compareTo(other) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override public SyncToken clone() {
        if (mChangeOffsetInNext >= 0 && mDeleteModSeq >= 0) {
            return new SyncToken(mChangeId, mChangeOffsetInNext, mDeleteModSeq, mDeleteOffsetInNext);
        } else if (mChangeOffsetInNext >= 0) {
            return new SyncToken(mChangeId, mChangeOffsetInNext);
        } else {
            return new SyncToken(mChangeId);
        }
    }

    public int compareTo(Object arg0) {
        SyncToken other = (SyncToken) arg0;
        int diff = this.mChangeId - other.mChangeId;
        if (diff == 0) {
            int delDiff = this.mDeleteModSeq - other.mDeleteModSeq;
            if (delDiff == 0) {
                if ((this.mChangeOffsetInNext == -1 && other.mChangeOffsetInNext == -1) &&
                    (this.mDeleteOffsetInNext == -1 && other.mDeleteOffsetInNext == -1)) {
                    return 0;
                } else if ((this.mChangeOffsetInNext >= 0 || this.mDeleteOffsetInNext >= 0) &&
                    other.mChangeOffsetInNext == -1 && other.mDeleteOffsetInNext == -1) {
                    return 1;
                } else if (this.mChangeOffsetInNext == -1 && this.mDeleteOffsetInNext == -1 &&
                    (other.mChangeOffsetInNext >= 0 || other.mDeleteOffsetInNext >= 0)) {
                    return -1;
                } else {
                    int diffChangeOffset = this.mChangeOffsetInNext - other.mChangeOffsetInNext;
                    int diffDeleteOffset = this.mDeleteOffsetInNext - other.mDeleteOffsetInNext;
                    if (diffChangeOffset != 0) {
                        return diffChangeOffset;
                    } else {
                        return diffDeleteOffset;
                    }
                }
            } else {
                return delDiff;
            }
        } else {
            return diff;
        }
    }
}
