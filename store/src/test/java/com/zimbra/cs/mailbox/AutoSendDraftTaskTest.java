// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mime.ParsedMessage;
import java.util.Collection;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AutoSendDraftTask}.
 *
 * <p>Covers all entries in {@code PERMANENT_SEND_FAILURE_CODES}:
 * <ul>
 *   <li>{@link MailServiceException#SEND_PARTIAL_ADDRESS_FAILURE} — partial delivery,
 *       invalid recipient(s), {@code sendPartial=true}</li>
 *   <li>{@link MailServiceException#SEND_ABORTED_ADDRESS_FAILURE} — nothing delivered,
 *       all recipients invalid</li>
 *   <li>{@link MailServiceException#MESSAGE_TOO_BIG} — message exceeds MTA size limit</li>
 * </ul>
 * as well as transient failures (must be re-thrown) and the success path.
 */
class AutoSendDraftTaskTest extends MailboxTestSuite {

  private Mailbox mbox;

  /**
   * Builds an {@link AutoSendDraftTask} for the given mailbox+draft, injecting {@code customSender}
   * via the {@link AutoSendDraftTask#getMailSender(Mailbox)} hook. The hook is used for the draft
   * send; on permanent address failure the autoSendTime is cleared to stop the retry loop.
   */
  private static AutoSendDraftTask taskWithSender(
      int mailboxId, int draftId, MailSender customSender) {
    AutoSendDraftTask task =
        new AutoSendDraftTask() {
          @Override
          protected MailSender getMailSender(Mailbox mbox) {
            return customSender;
          }
        };
    task.setMailboxId(mailboxId);
    task.setProperty("draftId", Integer.toString(draftId));
    return task;
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  /**
   * Returns a {@link MailSender} whose {@link MailSender#sendMessage} always throws a {@link
   * MailSender.SafeSendFailedException} with one invalid address, simulating a permanent MTA
   * rejection.
   *
   * <p>Because {@link AutoSendDraftTask} always sets {@code sendPartial=true} before calling {@link
   * MailSender#sendMimeMessage}, the exception will be wrapped as {@link
   * MailServiceException#SEND_PARTIAL_ADDRESS_FAILURE} by {@link MailSender#sendMimeMessage}.
   */
  @SuppressWarnings("SameParameterValue")
  private static MailSender senderThatRejectsRecipient(String invalidAddress) {
    return new MailSender() {
      @Override
      protected Collection<Address> sendMessage(
          Mailbox mbox, MimeMessage mm, Collection<RollbackData> rollbacks)
          throws SafeMessagingException {
        Address invalid = null;
        try {
          invalid = new InternetAddress(invalidAddress);
        } catch (AddressException ignored) {
        }
        throw new SafeSendFailedException(
            new SendFailedException(
                "MESSAGE_NOT_DELIVERED",
                new Exception("RCPT failed: Invalid recipient"),
                new Address[0], // valid sent
                new Address[0], // valid unsent
                new Address[] {invalid} // invalid
                ));
      }
    };
  }

  @BeforeEach
  void setUp() throws Exception {
    Account account = createAccount().create();
    mbox = MailboxManager.getInstance().getMailboxByAccount(account);
  }

  /**
   * Creates a draft {@link Message} in the mailbox with a scheduled {@code autoSendTime} using the
   * correct {@link Mailbox#saveDraft} overload that persists the auto-send metadata.
   */
  private Message createScheduledDraft(long autoSendTime) throws Exception {
    MimeMessage mm = new MimeMessage(Session.getInstance(new Properties()));
    mm.setSubject("Send Later test");
    mm.setRecipient(
        MimeMessage.RecipientType.TO, new InternetAddress("recipient@external.example.com"));
    mm.setText("body");
    mm.saveChanges();

    ParsedMessage pm = new ParsedMessage(mm, false);
    return mbox.saveDraft(
        null, pm, Mailbox.ID_AUTO_INCREMENT, null, null, null, null, autoSendTime);
  }

  // ── tests ─────────────────────────────────────────────────────────────────

  /**
   * When {@link MailSender#sendMimeMessage} throws {@link
   * MailServiceException#SEND_PARTIAL_ADDRESS_FAILURE} (which is what happens when {@code
   * sendPartial=true} and at least one RCPT is rejected by the MTA with a 5xx error), the task
   * must:
   *
   * <ol>
   *   <li>NOT propagate the exception (which would cause the task scheduler to retry).
   *   <li>Delete the draft, because valid recipients already received the message and keeping the
   *       draft would risk an accidental duplicate send.
   * </ol>
   *
   * <p>This covers the primary SendLater infinite-retry-loop scenario: an internal domain recipient
   * that does not exist is rejected with 550 5.1.1 by the MTA.
   */
  @Test
  @DisplayName("call() should delete draft and not retry on SEND_PARTIAL_ADDRESS_FAILURE")
  void call_shouldDeleteDraftAndNotRetry_whenSendPartialAddressFailure()
      throws Exception {
    long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
    Message draft = createScheduledDraft(futureAutoSendTime);
    int draftId = draft.getId();

    assertEquals(
        futureAutoSendTime,
        draft.getDraftAutoSendTime(),
        "Pre-condition: draft should have autoSendTime set");

    MailSender failingSender = senderThatRejectsRecipient("bad-user@internal.domain");
    AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, failingSender);

    // Must NOT throw — permanent failures must be handled, not propagated.
    assertDoesNotThrow(task::call);

    // Draft must be deleted: valid recipients already received the message, keeping the draft
    // would risk an accidental duplicate send.
    assertThrows(
        MailServiceException.NoSuchItemException.class,
        () -> mbox.getItemById(null, draftId, MailItem.Type.MESSAGE),
        "Draft should be deleted after partial send (valid recipients already received it)");
  }

  /**
   * Same behavior for {@link MailServiceException#SEND_ABORTED_ADDRESS_FAILURE}: when ALL
   * recipients are rejected the MTA aborts before DATA, so nothing is delivered. Even though the
   * task sets {@code sendPartial=true}, the aborted-failure code is still part of
   * {@code PERMANENT_SEND_FAILURE_CODES} and must be handled identically — clear
   * {@code autoSendTime} and stop retrying.
   */
  @Test
  @DisplayName("call() should clear autoSendTime and not retry on SEND_ABORTED_ADDRESS_FAILURE")
  void call_shouldClearAutoSendTimeAndNotRetry_whenSendAbortedAddressFailure()
      throws Exception {
    long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
    Message draft = createScheduledDraft(futureAutoSendTime);
    int draftId = draft.getId();

    // Inject a sender that throws SEND_ABORTED_ADDRESS_FAILURE directly, bypassing sendMessage,
    // to cover the code path where all RCPT commands fail before DATA is sent.
    Address[] invalid = new Address[] {new InternetAddress("bad@internal.domain")};
    MailServiceException abortedEx =
        MailServiceException.SEND_ABORTED_ADDRESS_FAILURE(
            "Invalid address: bad@internal.domain", null, invalid, new Address[0]);

    MailSender abortingSender =
        new MailSender() {
          @Override
          protected Collection<Address> sendMessage(
              Mailbox mbox, MimeMessage mm, Collection<RollbackData> rollbacks) {
            // Never reached because sendMimeMessage will throw before calling sendMessage
            // in some code paths, but we override the whole sender for safety.
            return java.util.Collections.emptyList();
          }

          @Override
          public com.zimbra.cs.service.util.ItemId sendMimeMessage(
              OperationContext octxt, Mailbox mbox, javax.mail.internet.MimeMessage mm)
              throws com.zimbra.common.service.ServiceException {
            throw abortedEx;
          }
        };

    AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, abortingSender);

    assertDoesNotThrow(task::call);

    Message draftAfter = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
    assertNotNull(draftAfter, "Draft should still exist in the mailbox");
    assertEquals(
        0L,
        draftAfter.getDraftAutoSendTime(),
        "autoSendTime must be cleared to 0 to stop the retry loop");
  }

  /**
   * {@link MailServiceException#MESSAGE_TOO_BIG} is in {@code PERMANENT_SEND_FAILURE_CODES}:
   * the message size cannot change between retries, so retrying would always produce the same
   * result and would cause the same infinite-retry-loop problem as invalid recipient failures.
   * The task must clear {@code autoSendTime} and stop without propagating the exception.
   */
  @Test
  @DisplayName("call() should clear autoSendTime and not retry on MESSAGE_TOO_BIG")
  void call_shouldClearAutoSendTimeAndNotRetry_whenMessageTooBig() throws Exception {
    long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
    Message draft = createScheduledDraft(futureAutoSendTime);
    int draftId = draft.getId();

    MailServiceException tooBigEx = MailServiceException.MESSAGE_TOO_BIG(1024L, 2048L);

    MailSender tooBigSender =
        new MailSender() {
          @Override
          protected Collection<Address> sendMessage(
              Mailbox mbox, MimeMessage mm, Collection<RollbackData> rollbacks) {
            return java.util.Collections.emptyList();
          }

          @Override
          public com.zimbra.cs.service.util.ItemId sendMimeMessage(
              OperationContext octxt, Mailbox mbox, javax.mail.internet.MimeMessage mm)
              throws com.zimbra.common.service.ServiceException {
            throw tooBigEx;
          }
        };

    AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, tooBigSender);

    // Must NOT throw — permanent failures must be handled, not propagated.
    assertDoesNotThrow(task::call);

    // autoSendTime must be cleared to prevent the task from being rescheduled.
    Message draftAfter = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
    assertNotNull(draftAfter, "Draft should still exist in the mailbox");
    assertEquals(
        0L,
        draftAfter.getDraftAutoSendTime(),
        "autoSendTime must be cleared to 0 to stop the retry loop");
  }

  /**
   * A {@link MailServiceException} whose code is NOT in {@code PERMANENT_SEND_FAILURE_CODES}
   * (e.g. {@link MailServiceException#TRY_AGAIN} for a transient MTA connectivity failure) must
   * be re-thrown so the task scheduler can decide to retry the task. The draft's
   * {@code autoSendTime} must be left unchanged.
   */
  @Test
  @DisplayName("call() should re-throw non-permanent ServiceExceptions so the scheduler can retry")
  void call_shouldRethrow_whenTransientServiceExceptionOccurs() throws Exception {
    long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
    Message draft = createScheduledDraft(futureAutoSendTime);
    int draftId = draft.getId();

    MailServiceException transientEx = MailServiceException.TRY_AGAIN("MTA unreachable");

    MailSender transientSender =
        new MailSender() {
          @Override
          protected Collection<Address> sendMessage(
              Mailbox mbox, MimeMessage mm, Collection<RollbackData> rollbacks) {
            return java.util.Collections.emptyList();
          }

          @Override
          public com.zimbra.cs.service.util.ItemId sendMimeMessage(
              OperationContext octxt, Mailbox mbox, javax.mail.internet.MimeMessage mm)
              throws com.zimbra.common.service.ServiceException {
            throw transientEx;
          }
        };

    AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, transientSender);

    // Transient failures must propagate so the scheduler can retry.
    assertThrows(MailServiceException.class, task::call);

    // autoSendTime must NOT have been cleared — the draft should still be scheduled.
    Message draftAfter = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
    assertEquals(
        futureAutoSendTime,
        draftAfter.getDraftAutoSendTime(),
        "autoSendTime must be preserved when a transient (retryable) failure occurs");
  }

  /** After a successful send the draft must be deleted from the mailbox. */
  @Test
  @DisplayName("call() should delete the draft after a successful send")
  void call_shouldDeleteDraft_whenSendSucceeds() throws Exception {
    long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
    Message draft = createScheduledDraft(futureAutoSendTime);
    int draftId = draft.getId();

    MailSender successSender =
        new MailSender() {
          @Override
          protected Collection<Address> sendMessage(
              Mailbox mbox, MimeMessage mm, Collection<RollbackData> rollbacks) {
            try {
              return java.util.List.of(new InternetAddress("recipient@external.example.com"));
            } catch (Exception e) {
              return java.util.Collections.emptyList();
            }
          }
        };

    AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, successSender);

    task.call();

    // Draft must be gone after a successful send.
    assertThrows(
        MailServiceException.NoSuchItemException.class,
        () -> mbox.getItemById(null, draftId, MailItem.Type.MESSAGE),
        "Draft should be deleted after successful send");
  }
}
