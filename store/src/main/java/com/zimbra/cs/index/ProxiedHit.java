// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.base.Strings;
import com.zimbra.common.mailbox.MailItemType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.service.util.ItemId;

/**
 * A {@link ZimbraHit} which is being proxied from another server: i.e. we did a SOAP request
 * somewhere else and are now wrapping results we got from request.
 *
 * @since Mar 28, 2005
 * @author tim
 */
public class ProxiedHit extends ZimbraHit {
  private int proxiedConvId = -1;
  private int proxiedMsgId = -1;
  private ItemId itemId;
  private final Element element;

  public ProxiedHit(ZimbraQueryResultsImpl results, Element elt, Object sortValue) {
    super(results, null, sortValue);
    element = elt;
  }

  @Override
  public ItemId getParsedItemID() throws ServiceException {
    if (itemId == null) {
      itemId = new ItemId(element.getAttribute(MailConstants.A_ID), (String) null);
    }
    return itemId;
  }

  void setParsedItemId(ItemId value) {
    itemId = value;
  }

  @Override
  int getConversationId() throws ServiceException {
    if (proxiedConvId <= 0) {
      proxiedConvId = (int) element.getAttributeLong(MailConstants.A_CONV_ID, 0);
    }
    return proxiedConvId;
  }

  @Override
  public MailItem getMailItem() {
    return null;
  }

  @Override
  public MailItemType getMailItemType() throws ServiceException {
    String elemName = element.getName();
    if (MailConstants.E_CONV.equals(elemName)) {
      return MailItemType.CONVERSATION;
    } else if (MailConstants.E_MSG.equals(elemName)) {
      return MailItemType.MESSAGE;
    } else if (MailConstants.E_CHAT.equals(elemName)) {
      return MailItemType.CHAT;
    } else if (MailConstants.E_CONTACT.equals(elemName)) {
      return MailItemType.CONTACT;
    } else if (MailConstants.E_APPOINTMENT.equals(elemName)) {
      return MailItemType.APPOINTMENT;
    }
    return MailItemType.UNKNOWN;
  }

  @Override
  public int getImapUid() throws ServiceException {
    return element.getAttributeInt(MailConstants.A_IMAP_UID, proxiedMsgId);
  }

  @Override
  public int getFlagBitmask() throws ServiceException {
    String flags = element.getAttribute(MailConstants.A_FLAGS, null);
    return Flag.toBitmask(flags);
  }

  @Override
  public String[] getTags() throws ServiceException {
    String tn = element.getAttribute(MailConstants.A_TAG_NAMES, null);
    if (Strings.isNullOrEmpty(tn)) {
      return new String[] {};
    }
    return tn.split(",");
  }

  @Override
  public int getItemId() throws ServiceException {
    if (proxiedMsgId <= 0) {
      proxiedMsgId = getParsedItemID().getId();
    }
    return proxiedMsgId;
  }

  @Override
  void setItem(MailItem item) {
    assert (false); // can't preload a proxied hit!
  }

  @Override
  boolean itemIsLoaded() {
    return true;
  }

  String getFragment() {
    Element frag = element.getOptionalElement(MailConstants.E_FRAG);
    return frag != null ? frag.getText() : "";
  }

  @Override
  String getName() throws ServiceException {
    return element.getAttribute(MailConstants.A_SORT_FIELD);
  }

  @Override
  public String toString() {
    return element.toString();
  }

  public String getServer() {
    return ((ProxiedQueryResults) getResults()).getServer();
  }

  public Element getElement() {
    return element;
  }

  @Override
  boolean isLocal() {
    return false;
  }
}
