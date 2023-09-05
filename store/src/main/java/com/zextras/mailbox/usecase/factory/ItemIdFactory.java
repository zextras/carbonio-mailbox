package com.zextras.mailbox.usecase.factory;

import com.zimbra.cs.service.util.ItemId;

public interface ItemIdFactory {
  ItemId create(String folderId, String defaultAccountId);

  // ItemId create(int folderId, String defaultAccountId);
}
