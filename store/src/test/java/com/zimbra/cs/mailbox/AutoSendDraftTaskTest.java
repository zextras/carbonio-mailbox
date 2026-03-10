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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AutoSendDraftTask}
 */
class AutoSendDraftTaskTest extends MailboxTestSuite {

    private Mailbox mbox;

    /**
     * Builds an {@link AutoSendDraftTask} for the given mailbox+draft, injecting
     * {@code customSender} via the {@link AutoSendDraftTask#getMailSender(Mailbox)} hook.
     * The hook is used both for the draft send and (on failure) the NDR send.
     */
    private static AutoSendDraftTask taskWithSender(int mailboxId, int draftId,
            MailSender customSender) {
        AutoSendDraftTask task = new AutoSendDraftTask() {
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
     * Returns a {@link MailSender} whose {@link MailSender#sendMessage} always throws a
     * {@link MailSender.SafeSendFailedException} with one invalid address, simulating a permanent
     * MTA rejection.
     *
     * <p>Because {@link AutoSendDraftTask} always sets {@code sendPartial=true} before calling
     * {@link MailSender#sendMimeMessage}, the exception will be wrapped as
     * {@link MailServiceException#SEND_PARTIAL_ADDRESS_FAILURE} by
     * {@link MailSender#sendMimeMessage}.
     */
    @SuppressWarnings("SameParameterValue")
    private static MailSender senderThatRejectsRecipient(String invalidAddress) {
        return new MailSender() {
            @Override
            protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
                    Collection<RollbackData> rollbacks)
                    throws SafeMessagingException {
                Address invalid = new InternetAddress() {{
                    try { setAddress(invalidAddress); } catch (Exception ignored) {}
                }};
                throw new SafeSendFailedException(
                        new SendFailedException("MESSAGE_NOT_DELIVERED",
                                new Exception("RCPT failed: Invalid recipient"),
                                new Address[0],         // valid sent
                                new Address[0],         // valid unsent
                                new Address[]{invalid}  // invalid
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
     * Creates a draft {@link Message} in the mailbox with a scheduled {@code autoSendTime} using
     * the correct {@link Mailbox#saveDraft} overload that persists the auto-send metadata.
     */
    private Message createScheduledDraft(long autoSendTime) throws Exception {
        MimeMessage mm = new MimeMessage(Session.getInstance(new Properties()));
        mm.setSubject("Send Later test");
        mm.setRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress("recipient@external.example.com"));
        mm.setText("body");
        mm.saveChanges();

        ParsedMessage pm = new ParsedMessage(mm, false);
        return mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT,
                null, null, null, null, autoSendTime);
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    /**
     * When {@link MailSender#sendMimeMessage} throws
     * {@link MailServiceException#SEND_PARTIAL_ADDRESS_FAILURE} (which is what happens when
     * {@code sendPartial=true} and at least one RCPT is rejected), the task must:
     * <ol>
     *   <li>NOT propagate the exception (which would cause the task scheduler to retry).</li>
     *   <li>Clear the draft's {@code autoSendTime} to 0 so the task is not re-scheduled on
     *       server restart.</li>
     *   <li>Leave the draft message intact so the user can correct the recipient.</li>
     * </ol>
     */
    @Test
    @DisplayName("call() should clear autoSendTime and not retry when send fails due to invalid recipient")
    void call_shouldClearAutoSendTimeAndNotRetry_whenSendAbortedDueToInvalidRecipient()
            throws Exception {
        long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
        Message draft = createScheduledDraft(futureAutoSendTime);
        int draftId = draft.getId();

        assertEquals(futureAutoSendTime, draft.getDraftAutoSendTime(),
                "Pre-condition: draft should have autoSendTime set");

        MailSender failingSender = senderThatRejectsRecipient("bad-user@internal.domain");
        AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, failingSender);

        // Must NOT throw — permanent failures must be handled, not propagated.
        assertDoesNotThrow(task::call);

        // autoSendTime must be cleared to prevent the task from being rescheduled.
        Message draftAfter = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
        assertNotNull(draftAfter, "Draft should still exist in the mailbox");
        assertEquals(0L, draftAfter.getDraftAutoSendTime(),
                "autoSendTime must be cleared to 0 to stop the retry loop");
    }

    /**
     * Same behavior for {@link MailServiceException#SEND_ABORTED_ADDRESS_FAILURE}: even though
     * the task sets {@code sendPartial=true}, if all recipients are invalid the MTA may still
     * produce an aborted failure. The task must handle both codes the same way.
     */
    @Test
    @DisplayName("call() should clear autoSendTime and not retry when all recipients are invalid (aborted)")
    void call_shouldClearAutoSendTimeAndNotRetry_whenSendPartialDueToInvalidRecipient()
            throws Exception {
        long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
        Message draft = createScheduledDraft(futureAutoSendTime);
        int draftId = draft.getId();

        // Inject a sender that throws SEND_ABORTED_ADDRESS_FAILURE directly, bypassing sendMessage,
        // to cover the code path where all RCPT commands fail before DATA is sent.
        Address[] invalid = new Address[]{new InternetAddress("bad@internal.domain")};
        MailServiceException abortedEx = MailServiceException.SEND_ABORTED_ADDRESS_FAILURE(
                "Invalid address: bad@internal.domain", null, invalid, new Address[0]);

        MailSender abortingSender = new MailSender() {
            @Override
            protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
                    Collection<RollbackData> rollbacks) {
                // Never reached because sendMimeMessage will throw before calling sendMessage
                // in some code paths, but we override the whole sender for safety.
                return java.util.Collections.emptyList();
            }

            @Override
            public com.zimbra.cs.service.util.ItemId sendMimeMessage(
                    OperationContext octxt, Mailbox mbox,
                    javax.mail.internet.MimeMessage mm) throws com.zimbra.common.service.ServiceException {
                throw abortedEx;
            }
        };

        AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, abortingSender);

        assertDoesNotThrow(task::call);

        Message draftAfter = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
        assertNotNull(draftAfter, "Draft should still exist in the mailbox");
        assertEquals(0L, draftAfter.getDraftAutoSendTime(),
                "autoSendTime must be cleared to 0 to stop the retry loop");
    }


    /**
     * A non-address-related {@link MailServiceException} (e.g. a transient connectivity failure)
     * must be re-thrown so the task scheduler can decide to retry the task.
     */
    @Test
    @DisplayName("call() should re-throw non-address ServiceExceptions so the scheduler can retry")
    void call_shouldRethrow_whenTransientServiceExceptionOccurs() throws Exception {
        long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
        Message draft = createScheduledDraft(futureAutoSendTime);
        int draftId = draft.getId();

        MailServiceException transientEx = MailServiceException.TRY_AGAIN("MTA unreachable");

        MailSender transientSender = new MailSender() {
            @Override
            protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
                    Collection<RollbackData> rollbacks) {
                return java.util.Collections.emptyList();
            }

            @Override
            public com.zimbra.cs.service.util.ItemId sendMimeMessage(
                    OperationContext octxt, Mailbox mbox,
                    javax.mail.internet.MimeMessage mm) throws com.zimbra.common.service.ServiceException {
                throw transientEx;
            }
        };

        AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, transientSender);

        // Transient failures must propagate so the scheduler can retry.
        assertThrows(MailServiceException.class, task::call);

        // autoSendTime must NOT have been cleared — the draft should still be scheduled.
        Message draftAfter = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
        assertEquals(futureAutoSendTime, draftAfter.getDraftAutoSendTime(),
                "autoSendTime must be preserved when a transient (retryable) failure occurs");
    }

    /**
     * After a successful send the draft must be deleted from the mailbox.
     */
    @Test
    @DisplayName("call() should delete the draft after a successful send")
    void call_shouldDeleteDraft_whenSendSucceeds() throws Exception {
        long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
        Message draft = createScheduledDraft(futureAutoSendTime);
        int draftId = draft.getId();

        MailSender successSender = new MailSender() {
            @Override
            protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
                    Collection<RollbackData> rollbacks) {
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
        assertThrows(MailServiceException.NoSuchItemException.class,
                () -> mbox.getItemById(null, draftId, MailItem.Type.MESSAGE),
                "Draft should be deleted after successful send");
    }
}

