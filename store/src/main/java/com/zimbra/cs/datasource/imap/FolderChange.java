// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource.imap;

import com.zimbra.cs.mailbox.Folder;

class FolderChange {
  public enum Type {
    ADDED,
    MOVED,
    DELETED
  }

  private final Type type;
  private final int itemId;
  private final Folder folder;
  private final ImapFolder tracker;

  public static FolderChange added(Folder folder) {
    return new FolderChange(Type.ADDED, folder.getId(), folder, null);
  }

  public static FolderChange moved(Folder folder, ImapFolder tracker) {
    return new FolderChange(Type.MOVED, folder.getId(), folder, tracker);
  }

  public static FolderChange deleted(int itemId, ImapFolder tracker) {
    return new FolderChange(Type.DELETED, itemId, null, tracker);
  }

  private FolderChange(Type type, int itemId, Folder folder, ImapFolder tracker) {
    this.type = type;
    this.itemId = itemId;
    this.folder = folder;
    this.tracker = tracker;
  }

  public Type getType() {
    return type;
  }

  public int getItemId() {
    return itemId;
  }

  public Folder getFolder() {
    return folder;
  }

  public ImapFolder getTracker() {
    return tracker;
  }

  public boolean isAdded() {
    return type == Type.ADDED;
  }

  public boolean isMoved() {
    return type == Type.MOVED;
  }

  public boolean isDeleted() {
    return type == Type.DELETED;
  }
}
