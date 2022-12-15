// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource.imap;

import com.zimbra.cs.datasource.imap.ImapFolder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImapFolderCollection implements Iterable<ImapFolder> {
    private final Map<Integer, ImapFolder> mByItemId;
    private final Map<String, ImapFolder> mByLocalPath;
    private final Map<String, ImapFolder> mByRemotePath;

    public ImapFolderCollection() {
        mByItemId = new HashMap<Integer, ImapFolder>();
        mByLocalPath = new HashMap<String, ImapFolder>();
        mByRemotePath = new HashMap<String, ImapFolder>();
    }

    public void add(ImapFolder imapFolder) {
        mByLocalPath.put(imapFolder.getLocalPath().toLowerCase(), imapFolder);
        mByRemotePath.put(imapFolder.getRemoteId(), imapFolder);
        mByItemId.put(imapFolder.getItemId(), imapFolder);
    }
    
    public void remove(ImapFolder imapFolder) {
        mByLocalPath.remove(imapFolder.getLocalPath().toLowerCase());
        mByRemotePath.remove(imapFolder.getRemoteId());
        mByItemId.remove(imapFolder.getItemId());
    }
    
    public ImapFolder getByLocalPath(String localPath) {
        return mByLocalPath.get(localPath.toLowerCase());
    }
    
    public ImapFolder getByRemotePath(String remotePath) {
        return mByRemotePath.get(remotePath);
    }
    
    public ImapFolder getByItemId(int itemId) {
        return mByItemId.get(itemId);
    }
    
    public int size() {
        return mByItemId.size();
    }

    public Iterator<ImapFolder> iterator() {
        return mByItemId.values().iterator();
    }
}
