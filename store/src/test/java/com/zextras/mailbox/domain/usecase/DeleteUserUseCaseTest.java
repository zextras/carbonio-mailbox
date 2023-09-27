package com.zextras.mailbox.domain.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.util.Log;
import com.zimbra.cs.account.*;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import io.vavr.control.Try;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;

@Execution(ExecutionMode.CONCURRENT)
public class DeleteUserUseCaseTest {

  private MailboxManager mockMailboxManager;
  private Log mockZimbraSecurityLog;
  private DeleteUserUseCase deleteUserUseCase;
  private Provisioning mockProvisioning;
  private File localConfig;
  private File tempDir;

  @BeforeEach
  void setUp() {
    mockProvisioning = mock(Provisioning.class);
    mockMailboxManager = mock(MailboxManager.class);
    mockZimbraSecurityLog = mock(Log.class);
    deleteUserUseCase =
        new DeleteUserUseCase(mockProvisioning, mockMailboxManager, mockZimbraSecurityLog);
  }

  @Test
  void shouldReturnSuccessWhenUserDeleted() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);
    assertTrue(deleteResult.isSuccess());
    assertNull(deleteResult.get());
  }

  @Test
  void shouldReturnFailureWhenUserDoesntExist() throws Exception {
    final String userId = "nonExistingUser";
    when(mockProvisioning.getAccountById(userId)).thenReturn(null);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);
    assertTrue(deleteResult.isFailure());
    final RuntimeException runtimeException =
        assertThrows(RuntimeException.class, deleteResult::get);

    assertEquals("User " + userId + " doesn't exist", runtimeException.getMessage());
  }

  @Test
  void shouldPutAccountInMaintenanceModeBeforeDeletion() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);

    assertTrue(deleteResult.isSuccess());

    final String expectedStatusKind = ZAttrProvisioning.AccountStatus.maintenance.name();
    final ArgumentCaptor<String> gotStatusKind = ArgumentCaptor.forClass(String.class);
    verify(mockProvisioning).modifyAccountStatus(eq(mockAccount), gotStatusKind.capture());
    assertEquals(expectedStatusKind, gotStatusKind.getValue());
  }

  @Test
  void shouldDeleteMailboxIfAccountOnLocalServer() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    final Mailbox mockMailbox = mock(Mailbox.class);
    when(mockMailboxManager.getMailboxByAccount(mockAccount, false)).thenReturn(mockMailbox);
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);
    when(mockProvisioning.onLocalServer(mockAccount)).thenReturn(true);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);

    assertTrue(deleteResult.isSuccess());
    verify(mockMailbox, times(1)).deleteMailbox();
  }

  @Test
  void shouldNotDeleteMailboxIfAccountNotOnLocalServer() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    final Mailbox mockMailbox = mock(Mailbox.class);
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);
    when(mockProvisioning.onLocalServer(mockAccount)).thenReturn(false);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);

    assertTrue(deleteResult.isSuccess());
    verify(mockMailbox, times(0)).deleteMailbox();
  }

  @Test
  void shouldDeleteAccount() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    final Mailbox mockMailbox = mock(Mailbox.class);
    when(mockMailboxManager.getMailboxByAccount(mockAccount, false)).thenReturn(mockMailbox);
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);
    when(mockProvisioning.onLocalServer(mockAccount)).thenReturn(true);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);

    assertTrue(deleteResult.isSuccess());
    verify(mockProvisioning, times(1)).deleteAccount(userId);
  }

  @Test
  void shouldWriteDownToZimbraLogOnceAUserIsDelete() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    when(mockAccount.getName()).thenReturn("example");
    when(mockAccount.getId()).thenReturn(userId);
    final Mailbox mockMailbox = mock(Mailbox.class);

    when(mockMailboxManager.getMailboxByAccount(mockAccount, false)).thenReturn(mockMailbox);
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);
    when(mockProvisioning.onLocalServer(mockAccount)).thenReturn(true);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);

    assertTrue(deleteResult.isSuccess());
    final ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockZimbraSecurityLog, times(1)).info(logCaptor.capture());
    assertEquals("cmd=DeleteAccount; name=example; id=id123;", logCaptor.getValue());
  }
}
