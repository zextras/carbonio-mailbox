package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.midlewarepojo.GrantInput;
import com.zextras.mailbox.midlewarepojo.UpdateInput;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.accesscontrol.ACLHelper;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateFolderActionTest {
  private MailboxManager mailboxManager;
  private ItemIdFactory itemIdFactory;
  private UpdateFolderAction updateFolderAction;
  private Mailbox userMailbox;
  private OperationContext operationContext;
  private ItemId itemId;
  private ACLHelper aclHelper;
  private final String accountId = "account-id123";
  private final int realItemId = 1;
  private final String folderId = accountId + ":" + realItemId;
  private String internalGrantExpiryString;
  private String guestGrantExpiryString;
  private List<GrantInput> grantInputList;
  private String newName;
  private String flags;
  private byte color;
  private String view;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    userMailbox = mock(Mailbox.class);
    operationContext = mock(OperationContext.class);
    itemId = mock(ItemId.class);
    aclHelper = mock(ACLHelper.class);

    updateFolderAction = new UpdateFolderAction(mailboxManager, itemIdFactory, aclHelper);
  }

  private void setupDefaultMocks() throws ServiceException {
    when(itemId.getId()).thenReturn(realItemId);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(operationContext.getmRequestedAccountId()).thenReturn(accountId);
    when(itemId.belongsTo(userMailbox)).thenReturn(true);
  }

  @Test
  void shouldReturnFailureIfFolderItemIdDoesntBelongToUserMailbox() throws ServiceException {
    setupDefaultMocks();
    when(itemId.belongsTo(userMailbox)).thenReturn(false);
    final Try<Void> operationResult =
        updateFolderAction.update(
            operationContext,
            accountId,
            folderId,
            new UpdateInput.Builder()
                .setInternalGrantExpiryString(internalGrantExpiryString)
                .setGuestGrantExpiryString(guestGrantExpiryString)
                .setGrantInputList(grantInputList)
                .setNewName(newName)
                .setFlags(flags)
                .setColor(color)
                .setView(view)
                .build());

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be updated because folder doesn't belong to user mailbox");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(ServiceException.class, gotError);
    assertEquals("invalid request: cannot move folder between mailboxes", gotError.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void shouldThrowNoSuchFolderIfItemIdLessEqualThanZero(int id) throws ServiceException {
    final String folderId = accountId + ":" + id;
    setupDefaultMocks();
    when(itemId.getId()).thenReturn(id);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    final Try<Void> operationResult =
        updateFolderAction.update(
            operationContext,
            accountId,
            folderId,
            new UpdateInput.Builder()
                .setInternalGrantExpiryString(internalGrantExpiryString)
                .setGuestGrantExpiryString(guestGrantExpiryString)
                .setGrantInputList(grantInputList)
                .setNewName(newName)
                .setFlags(flags)
                .setColor(color)
                .setView(view)
                .build());

    assertTrue(operationResult.isFailure(), "Folder should not be updated because no such folder");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(MailServiceException.class, gotError);
    assertEquals("no such folder id: " + itemId.getId(), gotError.getMessage());
  }

  @Test
  void shouldBeSuccessAfterSetColor() throws ServiceException {
    color = 1;
    setupDefaultMocks();

    final Try<Void> operationResult =
        updateFolderAction.update(
            operationContext,
            accountId,
            folderId,
            new UpdateInput.Builder()
                .setInternalGrantExpiryString(internalGrantExpiryString)
                .setGuestGrantExpiryString(guestGrantExpiryString)
                .setGrantInputList(grantInputList)
                .setNewName(newName)
                .setFlags(flags)
                .setColor(color)
                .setView(view)
                .build());

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Color should be successfully set.");

    verify(userMailbox, times(1))
        .setColor(operationContext, itemId.getId(), MailItem.Type.FOLDER, color);
  }

  @Test
  void shouldBeSuccessAfterSetTags() throws ServiceException {
    flags = "flags";
    setupDefaultMocks();

    final Try<Void> operationResult =
        updateFolderAction.update(
            operationContext,
            accountId,
            folderId,
            new UpdateInput.Builder()
                .setInternalGrantExpiryString(internalGrantExpiryString)
                .setGuestGrantExpiryString(guestGrantExpiryString)
                .setGrantInputList(grantInputList)
                .setNewName(newName)
                .setFlags(flags)
                .setColor(color)
                .setView(view)
                .build());

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Tags should be successfully set.");

    verify(userMailbox, times(1))
        .setTags(
            operationContext,
            itemId.getId(),
            MailItem.Type.FOLDER,
            Flag.toBitmask(flags),
            null,
            null);
  }

  @Test
  void shouldBeSuccessAfterSetView() throws ServiceException {
    view = "view";
    setupDefaultMocks();

    final Try<Void> operationResult =
        updateFolderAction.update(
            operationContext,
            accountId,
            folderId,
            new UpdateInput.Builder()
                .setInternalGrantExpiryString(internalGrantExpiryString)
                .setGuestGrantExpiryString(guestGrantExpiryString)
                .setGrantInputList(grantInputList)
                .setNewName(newName)
                .setFlags(flags)
                .setColor(color)
                .setView(view)
                .build());

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "View should be successfully set.");

    verify(userMailbox, times(1))
        .setFolderDefaultView(operationContext, itemId.getId(), MailItem.Type.of(view));
  }

  @Test
  void shouldBeSuccessAfterRename() throws ServiceException {
    newName = "newName";
    setupDefaultMocks();
    final Try<Void> operationResult =
        updateFolderAction.update(
            operationContext,
            accountId,
            folderId,
            new UpdateInput.Builder()
                .setInternalGrantExpiryString(internalGrantExpiryString)
                .setGuestGrantExpiryString(guestGrantExpiryString)
                .setGrantInputList(grantInputList)
                .setNewName(newName)
                .setFlags(flags)
                .setColor(color)
                .setView(view)
                .build());

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "View should be successfully set.");

    verify(userMailbox, times(1))
        .rename(operationContext, itemId.getId(), MailItem.Type.FOLDER, newName, itemId.getId());
  }

  @Test
  void shouldBeSuccessAfterMove() throws ServiceException {
    setupDefaultMocks();

    final Try<Void> operationResult =
        updateFolderAction.update(
            operationContext,
            accountId,
            folderId,
            new UpdateInput.Builder()
                .setInternalGrantExpiryString(internalGrantExpiryString)
                .setGuestGrantExpiryString(guestGrantExpiryString)
                .setGrantInputList(grantInputList)
                .setNewName(newName)
                .setFlags(flags)
                .setColor(color)
                .setView(view)
                .build());

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "View should be successfully set.");

    verify(userMailbox, times(1))
        .move(operationContext, itemId.getId(), MailItem.Type.FOLDER, itemId.getId(), null);
  }

  @Test
  void shouldBeSuccessAfterSetPermission() throws ServiceException {
    internalGrantExpiryString = "internalGrantExpiry";
    guestGrantExpiryString = "guestGrantExpiry";
    grantInputList = List.of();

    ACL acl = mock(ACL.class);
    Account account = mock(Account.class);
    Folder folder = mock(Folder.class);
    setupDefaultMocks();
    when(userMailbox.getFolderById(operationContext, itemId.getId())).thenReturn(folder);
    when(folder.getDefaultView()).thenReturn(Type.FOLDER);

    when(userMailbox.getAccount()).thenReturn(account);
    when(aclHelper.parseACL(
            internalGrantExpiryString,
            guestGrantExpiryString,
            grantInputList,
            Type.FOLDER,
            account))
        .thenReturn(acl);

    final Try<Void> operationResult =
        updateFolderAction.update(
            operationContext,
            accountId,
            folderId,
            new UpdateInput.Builder()
                .setInternalGrantExpiryString(internalGrantExpiryString)
                .setGuestGrantExpiryString(guestGrantExpiryString)
                .setGrantInputList(grantInputList)
                .setNewName(newName)
                .setFlags(flags)
                .setColor(color)
                .setView(view)
                .build());

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "View should be successfully set.");

    verify(userMailbox, times(1)).setPermissions(operationContext, itemId.getId(), acl);
  }
}
