package com.zimbra.cs.service.servlet.preview;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.util.ItemId;

public class ItemIdFactory {
  public ItemId create(String itemId, String accountId) throws ServiceException {
    return new ItemId(itemId, accountId);
  }
}
