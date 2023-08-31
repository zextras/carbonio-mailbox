package com.zextras.mailbox.usecase;

import com.zimbra.cs.service.util.ItemId;

public interface ItemIdFactory {
  ItemId create(String folderId, String defaultAccountId);
}
