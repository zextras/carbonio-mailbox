// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

public enum MailItemType {
  UNKNOWN,
  FOLDER,
  /** Item is a standard Folder. */
  SEARCHFOLDER,
  /** Item is a saved search - SearchFolder. */
  TAG,
  /** Item is a user-created Tag. */
  CONVERSATION,
  /** Item is a real, persisted Conversation. */
  MESSAGE,
  /** Item is a mail Message. */
  CONTACT,
  /** Item is a Contact. */
  @Deprecated
  INVITE,
  /** Item is a InviteMessage with a {@code text/calendar} MIME part. */
  FLAG,
  /** Item is a memory-only system Flag. */
  APPOINTMENT,
  /** Item is a calendar Appointment. */
  VIRTUAL_CONVERSATION,
  /** Item is a memory-only, 1-message VirtualConversation. */
  MOUNTPOINT,
  /** Item is a Mountpoint pointing to a Folder, possibly in another user's Mailbox. */
  CHAT,
  /** Item is a Chat */
  CALENDAR_GROUP
  /** Item is a Calendar Group */
}
