package com.zextras.mailbox.usecase;

import com.zimbra.cs.mailbox.MailboxManager;
import javax.inject.Inject;

public class RefreshFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public RefreshFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }
}
