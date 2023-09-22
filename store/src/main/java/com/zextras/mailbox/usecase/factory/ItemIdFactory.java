package com.zextras.mailbox.usecase.factory;

import com.zimbra.cs.service.util.ItemId;

/**
 * Factory interface to create an {@link ItemId}.
 *
 * @author Davide Polonio
 * @since 23.10.0
 */
public interface ItemIdFactory {

  /**
   * Creates {@link ItemId by folder is and account id}.
   *
   * <p>If the folderId is in the format accountId:id, then the item is selected from accountId
   * mailbox.
   *
   * <p>If the folder is just an id, the item is selected from the defaultAccountId mailbox.
   *
   * @param folderId the id of the folder (belonging to the accountId)
   * @param defaultAccountId the target account zimbra id attribute
   * @return {@link ItemId}
   */
  ItemId create(String folderId, String defaultAccountId);
}
