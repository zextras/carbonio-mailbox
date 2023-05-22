// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.cs.mailbox.MailItem.CustomMetadata;
import java.util.Collection;

/** Specifies options for new messages created with {@link Mailbox#addMessage}. */
public class DeliveryOptions {

  private int mFolderId = -1;
  private boolean mNoICal = false;
  private int mFlags = 0;
  private String[] mTags = null;
  private int mConversationId = Mailbox.ID_AUTO_INCREMENT;
  private String mRecipientEmail = ":API:";
  private Message.DraftInfo mDraftInfo = null;
  private CustomMetadata mCustomMetadata = null;

  public int getFolderId() {
    return mFolderId;
  }

  public boolean getNoICal() {
    return mNoICal;
  }

  public int getFlags() {
    return mFlags;
  }

  public String[] getTags() {
    return mTags;
  }

  public int getConversationId() {
    return mConversationId;
  }

  public String getRecipientEmail() {
    return mRecipientEmail;
  }

  public Message.DraftInfo getDraftInfo() {
    return mDraftInfo;
  }

  public CustomMetadata getCustomMetadata() {
    return mCustomMetadata;
  }

  public DeliveryOptions setFolderId(int folderId) {
    mFolderId = folderId;
    return this;
  }

  public DeliveryOptions setFolderId(Folder folder) {
    mFolderId = folder.getId();
    return this;
  }

  public DeliveryOptions setNoICal(boolean noICal) {
    mNoICal = noICal;
    return this;
  }

  public DeliveryOptions setFlags(int flags) {
    mFlags = flags;
    return this;
  }

  public DeliveryOptions setTags(Collection<String> tags) {
    if (tags == null) {
      mTags = null;
    } else {
      mTags = tags.toArray(new String[0]);
    }
    return this;
  }

  public DeliveryOptions setTags(String[] tags) {
    mTags = tags;
    return this;
  }

  public DeliveryOptions setConversationId(int conversationId) {
    mConversationId = conversationId;
    return this;
  }

  public DeliveryOptions setRecipientEmail(String recipientEmail) {
    mRecipientEmail = recipientEmail;
    return this;
  }

  public DeliveryOptions setDraftInfo(Message.DraftInfo draftInfo) {
    mDraftInfo = draftInfo;
    return this;
  }

  public DeliveryOptions setCustomMetadata(CustomMetadata customMetadata) {
    mCustomMetadata = customMetadata;
    return this;
  }
}
