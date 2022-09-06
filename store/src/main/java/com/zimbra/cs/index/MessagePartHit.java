// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.base.MoreObjects;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import org.apache.lucene.document.Document;

/**
 * Inderect result object wrapped around Lucene {@link Document}.
 *
 * <p>You wouldn't think we'd need this -- but in fact there are situations where it is useful (e.g.
 * a query that ONLY uses MySQL and therefore has the real Conversation and Message objects) because
 * the Lucene {@link Document} isn't there.
 *
 * <p>Access to the real Lucene {@link Document} is perhaps not necessary here -- the few writable
 * APIs on the Lucene {@link Document} are probably not useful to us.
 *
 * @since Oct 15, 2004
 * @author tim
 */
public final class MessagePartHit extends ZimbraHit {

  private final Document document;
  private MessageHit hit;
  private final int itemId;

  protected MessagePartHit(
      ZimbraQueryResultsImpl res,
      Mailbox mbx,
      int id,
      Message msg,
      Document doc,
      Object sortValue) {
    super(res, mbx, sortValue);
    itemId = id;
    document = doc;
    if (msg != null) {
      getMessageResult(msg);
    }
  }

  @Override
  public int getConversationId() throws ServiceException {
    return getMessageResult().getConversationId();
  }

  @Override
  public String getName() throws ServiceException {
    if (cachedName == null) {
      cachedName = getMessageResult().getSender();
    }
    return cachedName;
  }

  @Override
  public int getItemId() {
    return itemId;
  }

  @Override
  void setItem(MailItem item) throws ServiceException {
    getMessageResult().setItem(item);
  }

  @Override
  boolean itemIsLoaded() throws ServiceException {
    return getMessageResult().itemIsLoaded();
  }

  @Override
  public String toString() {
    try {
      return MoreObjects.toStringHelper(this)
          .add("id", getItemId() + "-" + getPartName())
          .add("conv", getConversationId())
          .addValue(super.toString())
          .toString();
    } catch (ServiceException e) {
      return e.toString();
    }
  }

  public String getFilename() {
    return document != null ? document.get(LuceneFields.L_FILENAME) : null;
  }

  public String getType() {
    return document != null ? document.get(LuceneFields.L_MIMETYPE) : null;
  }

  public String getPartName() {
    if (document != null) {
      String part = document.get(LuceneFields.L_PARTNAME);
      if (part != null && !part.equals(LuceneFields.L_PARTNAME_TOP)) {
        return part;
      }
    }
    return "";
  }

  public MessageHit getMessageResult() {
    return getMessageResult(null);
  }

  /**
   * @return Message that contains this document
   */
  public MessageHit getMessageResult(Message msg) {
    if (hit == null) {
      hit = getResults().getMessageHit(getMailbox(), getItemId(), msg, document, sortValue);
      hit.addPart(this);
      hit.cacheImapMessage(cachedImapMessage);
      hit.cacheModifiedSequence(cachedModseq);
      hit.cacheParentId(cachedParentId);
    }
    return hit;
  }

  @Override
  public MailItem getMailItem() throws ServiceException {
    return getMessageResult().getMailItem();
  }
}
