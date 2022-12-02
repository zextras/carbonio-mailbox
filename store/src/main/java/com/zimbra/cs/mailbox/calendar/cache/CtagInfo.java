// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar.cache;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.client.ZFolder;
import com.zimbra.client.ZMountpoint;

// for CalDAV
// tracks last modified "time" of a calendar folder
// includes mountpoint link
public class CtagInfo {

    private int mId;            // id of this calendar folder
    private int mFolderId;      // id of parent folder
    private String mCtag;       // "ctag" that works like a last modified timestamp
    private String mPath;

    // applicable when this calendar is a mountpoint to a remote calendar
    private boolean mIsMountpoint;
    private String mRemoteAccount;  // mountpoint target account
    private int mRemoteId;          // id of the calendar folder in target account

    public int getId() { return mId; }
    public int getFolderId() { return mFolderId; }
    public String getCtag() { return mCtag; }
    public String getPath() { return mPath; }
    public boolean isMountpoint() { return mIsMountpoint; }
    public String getRemoteAccount() { return mRemoteAccount; }
    public int getRemoteId() { return mRemoteId; }

    CtagInfo(Folder calFolder) {
        boolean isMountpoint = calFolder instanceof Mountpoint;
        String remoteAccount = null;
        int remoteId = -1;
        if (isMountpoint) {
            Mountpoint mp = (Mountpoint) calFolder;
            remoteAccount = mp.getOwnerId();
            remoteId = mp.getRemoteId();
        }
        String ctag = makeCtag(calFolder);
        init(calFolder.getId(), calFolder.getFolderId(), ctag, calFolder.getPath(),
             isMountpoint, remoteAccount, remoteId);
    }

    CtagInfo(ZFolder calFolder) throws ServiceException {
        boolean isMountpoint = calFolder instanceof ZMountpoint;
        String remoteAccount = null;
        int remoteId = -1;
        if (isMountpoint) {
            ZMountpoint mp = (ZMountpoint) calFolder;
            remoteAccount = mp.getOwnerId();
            remoteId = Integer.parseInt(mp.getRemoteId());
        }
        String ctag = makeCtag(calFolder);

        ItemId iidId = new ItemId(calFolder.getId(), (String) null);
        int id = iidId.getId();
        int folderId = Mailbox.ID_FOLDER_USER_ROOT;
        String folderIdStr = calFolder.getParentId();
        if (folderIdStr != null && folderIdStr.length() > 0) {
            ItemId iidFolderId = new ItemId(folderIdStr, (String) null);
            folderId = iidFolderId.getId();
        }
        init(id, folderId, ctag, calFolder.getPath(), isMountpoint, remoteAccount, remoteId);
    }

    private void init(int id, int parentFolderId, String ctag, String path,
                      boolean isMountpoint, String remoteAccountId, int remoteId) {
        mId = id;
        mFolderId = parentFolderId;
        mCtag = ctag;
        mIsMountpoint = isMountpoint;
        if (mIsMountpoint) {
            mRemoteAccount = remoteAccountId;
            mRemoteId = remoteId;
        }
        if (path != null && !path.endsWith("/"))
            mPath = path + "/";
        else
            mPath = path;
    }

    void setCtag(String ctag) { mCtag = ctag; }

    private static String makeCtag(int modMetadata, int imapModseq) {
        return String.format("%d-%d", modMetadata, imapModseq);
    }

    public static String makeCtag(Folder calFolder) {
        return makeCtag(calFolder.getModifiedSequence(), calFolder.getImapMODSEQ());
    }

    public static String makeCtag(ZFolder calFolder) {
        return makeCtag(calFolder.getModifiedSequence(), calFolder.getImapMODSEQ());
    }

    private static final String FN_ID = "i";
    private static final String FN_FOLDER_ID = "f";
    private static final String FN_PATH = "p";
    private static final String FN_CTAG = "c";
    private static final String FN_REMOTE_ACCOUNT = "ra";
    private static final String FN_REMOTE_ID = "ri";

    Metadata encodeMetadata() {
        Metadata meta = new Metadata();
        meta.put(FN_ID, mId);
        meta.put(FN_FOLDER_ID, mFolderId);
        meta.put(FN_PATH, mPath);
        meta.put(FN_CTAG, mCtag);
        if (mIsMountpoint) {
            meta.put(FN_REMOTE_ACCOUNT, mRemoteAccount);
            meta.put(FN_REMOTE_ID, mRemoteId);
        }
        return meta;
    }

    CtagInfo(Metadata meta) throws ServiceException {
        int id = (int) meta.getLong(FN_ID, -1);
        int folderId = (int) meta.getLong(FN_FOLDER_ID, -1);
        String path = meta.get(FN_PATH, null);
        String ctag = meta.get(FN_CTAG, null);
        String remoteAccountId = meta.get(FN_REMOTE_ACCOUNT, null);
        int remoteId;
        boolean isMountpoint;
        if (remoteAccountId == null) {
            isMountpoint = false;
            remoteId = -1;
        } else {
            isMountpoint = true;
            remoteId = (int) meta.getLong(FN_REMOTE_ID, -1);
        }
        init(id, folderId, ctag, path, isMountpoint, remoteAccountId, remoteId);
    }
}
