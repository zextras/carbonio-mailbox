package com.zimbra.cs.service.servlet.preview;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.util.ItemId;

/**
 * A factory class for creating {@link ItemId} instances.
 * <p>
 * This class provides a method to create {@link ItemId} objects, encapsulating the logic
 * required to initialize the object with the given item ID and account ID.
 * </p>
 */
public class ItemIdFactory {

  /**
   * Creates a new {@link ItemId} instance with the specified item ID and account ID.
   * <p>
   * This method initializes an {@link ItemId} object using the provided parameters.
   * It throws a {@link ServiceException} if the creation of the {@link ItemId} fails.
   * </p>
   *
   * @param itemId    the unique identifier for the item
   * @param accountId the unique identifier for the account
   * @return a new {@link ItemId} instance
   * @throws ServiceException if an error occurs during the creation of the {@link ItemId}
   */
  public ItemId create(String itemId, String accountId) throws ServiceException {
    return new ItemId(itemId, accountId);
  }
}
