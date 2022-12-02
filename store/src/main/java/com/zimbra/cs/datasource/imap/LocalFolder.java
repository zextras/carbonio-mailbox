// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource.imap;

import java.util.HashSet;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailclient.imap.Flags;
import com.zimbra.cs.mailclient.imap.ListData;

final class LocalFolder {
    private final Mailbox mbox;
    private final String path;
    private Folder folder;
    private static final Log LOG = ZimbraLog.datasource;

    public static LocalFolder fromId(Mailbox mbox, int id) throws ServiceException {
        try {
            return new LocalFolder(mbox, mbox.getFolderById(null, id));
        } catch (MailServiceException.NoSuchItemException e) {
            return null;
        }
    }

    public static LocalFolder fromPath(Mailbox mbox, String path) throws ServiceException {
        try {
            return new LocalFolder(mbox, mbox.getFolderByPath(null, path));
        } catch (MailServiceException.NoSuchItemException e) {
            return null;
        }
    }

    LocalFolder(Mailbox mbox, String path) {
        this.mbox = mbox;
        this.path = path;
    }

    LocalFolder(Mailbox mbox, Folder folder) {
        this.mbox = mbox;
        this.path = folder.getPath();
        this.folder = folder;
    }

    public void delete() throws ServiceException {
        debug("deleting folder");
        try {
            getFolder();
        } catch (MailServiceException.NoSuchItemException e) {
            return;
        }
        mbox.delete(null, folder.getId(), folder.getType());
    }

    public void create() throws ServiceException {
        debug("creating folder");
        folder = mbox.createFolder(null, path, new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
    }

    public void updateFlags(ListData ld) throws ServiceException {
        getFolder();
        if (folder.getId() < 256) return; // Ignore system folder
        // debug("Updating flags (remote = %s)", ld.getMailbox());
        Flags flags = ld.getFlags();
        boolean noinferiors = flags.isNoinferiors() || ld.getDelimiter() == 0;
        int bits = folder.getFlagBitmask();
        if (((bits & Flag.BITMASK_NO_INFERIORS) != 0) != noinferiors) {
            debug("Setting NO_INFERIORS flag to " + noinferiors);
            alterTag(Flag.FlagInfo.NO_INFERIORS, noinferiors);
        }
        boolean sync = !flags.isNoselect();
        if (((bits & Flag.BITMASK_SYNCFOLDER) != 0) != sync) {
            debug("Setting sync flag to " + sync);
            alterTag(Flag.FlagInfo.SYNCFOLDER, sync);
            alterTag(Flag.FlagInfo.SYNC, sync);
        }
        if (folder.getDefaultView() != MailItem.Type.MESSAGE) {
            debug("Setting default view to TYPE_MESSAGE");
            mbox.setFolderDefaultView(null, folder.getId(), MailItem.Type.MESSAGE);
        }
    }

    public void alterTag(Flag.FlagInfo finfo, boolean value) throws ServiceException {
        mbox.alterTag(null, getFolder().getId(), MailItem.Type.FOLDER, finfo, value, null);
    }

    public void setMessageFlags(int id, int flagMask) throws ServiceException {
        mbox.setTags(null, id, MailItem.Type.MESSAGE, flagMask, MailItem.TAG_UNCHANGED);
    }

    public boolean exists() throws ServiceException {
        try {
            getFolder();
        } catch (MailServiceException.NoSuchItemException e) {
            return false;
        }
        return true;
    }

    public Message getMessage(int id) throws ServiceException {
        try {
            Message msg = mbox.getMessageById(null, id);
            if (msg.getFolderId() == getFolder().getId()) {
                return msg;
            }
        } catch (MailServiceException.NoSuchItemException e) {
        }
        return null;
    }

    public void deleteMessage(int id) throws ServiceException {
        debug("deleting message with id %d", id);
        try {
            mbox.delete(null, id, MailItem.Type.UNKNOWN);
        } catch (MailServiceException.NoSuchItemException e) {
            debug("message with id %d not found", id);
        }
    }

    public void emptyFolder() throws ServiceException {
        mbox.emptyFolder(null, getId(), false);
    }

    public Set<Integer> getMessageIds() throws ServiceException {
        return new HashSet<Integer>(mbox.listItemIds(null, MailItem.Type.MESSAGE, folder.getId()));
    }

    public Folder getFolder() throws ServiceException {
        if (folder == null) {
            folder = mbox.getFolderByPath(null, path);
        }
        return folder;
    }

    public int getId() throws ServiceException {
        return getFolder().getId();
    }

    public boolean isInbox() throws ServiceException {
        return getFolder().getId() == Mailbox.ID_FOLDER_INBOX;
    }

    public String getPath() {
        return folder != null ? folder.getPath() : path;
    }

    /*
     * Returns true if this is one of the known IMAP system folders
     * (e.g. INBOX, Sent). In this case, if we encounter a remote folder
     * with the same name then we will not map it to a unique local
     * folder name.
     */
    public boolean isKnown() {
        switch (folder.getId()) {
        case Mailbox.ID_FOLDER_INBOX:
        case Mailbox.ID_FOLDER_TRASH:
        case Mailbox.ID_FOLDER_SPAM:
        case Mailbox.ID_FOLDER_SENT:
        case Mailbox.ID_FOLDER_DRAFTS:
            return true;
        default:
            return false;
        }
    }

    public boolean isSystem() {
        return folder.getId() < Mailbox.FIRST_USER_ID;
    }

    public void debug(String fmt, Object... args) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(errmsg(String.format(fmt, args)));
        }
    }

    public void info(String fmt, Object... args) {
        LOG.info(errmsg(String.format(fmt, args)));
    }

    public void warn(String msg, Throwable e) {
        LOG.error(errmsg(msg), e);
    }

    public void error(String msg, Throwable e) {
        LOG.error(errmsg(msg), e);
    }

    private String errmsg(String s) {
        return String.format("Local folder '%s': %s", getPath(), s);
    }
}
