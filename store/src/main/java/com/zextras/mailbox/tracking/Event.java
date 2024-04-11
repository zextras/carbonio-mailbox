// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracking;

public class Event {

  public String getUserId() {
    return userId;
  }

  public String getCategory() {
    return category;
  }

  public String getAction() {
    return action;
  }

  private final String userId;
  private final String category;
  private final String action;

  public Event(String userId, String category, String action) {
    this.userId = userId;
    this.category = category;
    this.action = action;
  }
}
