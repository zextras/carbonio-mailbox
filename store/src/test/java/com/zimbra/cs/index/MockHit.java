// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.zimbra.cs.mailbox.MailItem;

/**
 * Mock implementation of {@link ZimbraHit} for testing.
 *
 * @author ysasaki
 */
public final class MockHit extends ZimbraHit {
  private int id;
  private int convId;
  private MailItem mailItem;

  public MockHit(ZimbraQueryResultsImpl results, int id, Object sortValue) {
    super(results, null, sortValue);
    this.id = id;
  }

  @Override
  public int getItemId() {
    return id;
  }

  public void setItemId(int value) {
    id = value;
  }

  @Override
  int getConversationId() {
    return convId;
  }

  void setConversationId(int value) {
    convId = value;
  }

  @Override
  public MailItem getMailItem() {
    return mailItem;
  }

  @Override
  void setItem(MailItem value) {
    mailItem = value;
  }

  @Override
  boolean itemIsLoaded() {
    return mailItem != null;
  }

  @Override
  String getName() {
    return (String) sortValue;
  }
}
