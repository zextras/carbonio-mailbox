// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Message.DraftInfo;
import java.util.Collection;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AutoSendDraftTask}.
 *
 * <p>Covers the bug where a scheduled "Send Later" draft with an invalid internal recipient caused
 * an infinite retry loop: the {@link com.zimbra.common.util.TaskScheduler} kept retrying the task
 * on every {@link ServiceException}, and the task was also re-loaded from the database on every
 * server restart, because the draft's {@code autoSendTime} was never cleared.
 */
class AutoSendDraftTaskTest extends MailboxTestSuite {

    private Mailbox mbox;

    @BeforeEach
    void setUp() throws Exception {
        Account account = createAccount().create();
        mbox = MailboxManager.getInstance().getMailboxByAccount(account);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a draft {@link Message} in the mailbox with a scheduled {@code autoSendTime}.
     */
    private Message createScheduledDraft(long autoSendTime) throws Exception {
        MimeMessage mm = new MimeMessage(Session.getInstance(new Properties()));
        mm.setSubject("Send Later test");
        mm.setRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress("recipient@external.example.com"));
        mm.setText("body");
        mm.saveChanges();

        DraftInfo draftInfo = new DraftInfo(null, null, null, null, autoSendTime);
        DeliveryOptions opts = new DeliveryOptions()
                .setFolderId(Mailbox.ID_FOLDER_DRAFTS)
                .setFlags(Flag.BITMASK_DRAFT);
        // DraftInfo must be passed as the explicit 5th argument; DeliveryOptions.setDraftInfo()
        // is silently ignored by the addMessage() overload chain.
        return mbox.addMessage(null,
                new com.zimbra.cs.mime.ParsedMessage(mm, false), opts, null, draftInfo);
    }

    /**
     * An {@link AutoSendDraftTask} subclass that injects a custom {@link MailSender} by
     * overriding {@link AutoSendDraftTask#getMailSender(Mailbox)}.  This avoids the need to spy
     * on {@link Mailbox} or stub {@link MailboxManager}.
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

    /**
     * A {@link MailSender} subclass that overrides {@link MailSender#sendMessage} to throw a
     * {@link SendFailedException} with one invalid address, simulating an invalid internal
     * recipient being rejected by the MTA.
     */
    @SuppressWarnings("SameParameterValue")
    private static MailSender senderThatRejectsRecipient(String invalidAddress) {
        return new MailSender() {
            @Override
            protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
                    Collection<RollbackData> rollbacks)
                    throws MailSender.SafeMessagingException {
                Address invalid = new InternetAddress() {
                    { try { setAddress(invalidAddress); } catch (Exception ignored) {} }
                };
                throw new MailSender.SafeSendFailedException(
                        new SendFailedException("MESSAGE_NOT_DELIVERED",
                                new Exception("RCPT failed: Invalid recipient"),
                                new Address[0],   // valid sent
                                new Address[0],   // valid unsent
                                new Address[]{invalid})); // invalid
            }
        };
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * When {@link MailSender#sendMimeMessage} throws {@link MailServiceException} with code
     * {@link MailServiceException#SEND_ABORTED_ADDRESS_FAILURE} (permanent, sender's fault),
     * the task must:
     * <ol>
     *   <li>NOT propagate the exception (which would cause TaskScheduler to retry).</li>
     *   <li>Clear the draft's {@code autoSendTime} to 0 so the task is not re-scheduled on
     *       restart.</li>
     *   <li>Leave the draft message intact in the mailbox so the user can correct it.</li>
     * </ol>
     */
    @Test
    void call_shouldClearAutoSendTimeAndNotRetry_whenSendAbortedDueToInvalidRecipient()
            throws Exception {
        // Arrange
        long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
        Message draft = createScheduledDraft(futureAutoSendTime);
        int draftId = draft.getId();

        assertEquals(futureAutoSendTime, draft.getDraftAutoSendTime(),
                "Pre-condition: draft should have autoSendTime set");

        // sendPartial=false → SEND_ABORTED_ADDRESS_FAILURE
        MailSender failingSender = senderThatRejectsRecipient("bad-user@internal.domain");
        AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, failingSender);

        // Act — must NOT throw
        task.call();

        // Assert
        Message draftAfter = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
        assertNotNull(draftAfter, "Draft should still exist in the mailbox");
        assertEquals(0L, draftAfter.getDraftAutoSendTime(),
                "autoSendTime must be cleared to 0 to prevent restart-loop re-scheduling");
    }

    /**
     * Same behavior for {@link MailServiceException#SEND_PARTIAL_ADDRESS_FAILURE}:
     * when {@code sendPartial=true} the {@link MailSender} throws
     * {@code SEND_PARTIAL_ADDRESS_FAILURE} instead of {@code SEND_ABORTED_ADDRESS_FAILURE},
     * but the task must still stop retrying and clear the autoSendTime.
     */
    @Test
    void call_shouldClearAutoSendTimeAndNotRetry_whenSendPartialDueToInvalidRecipient()
            throws Exception {
        // Arrange
        long futureAutoSendTime = System.currentTimeMillis() + 60_000L;
        Message draft = createScheduledDraft(futureAutoSendTime);
        int draftId = draft.getId();

        // sendPartial=true → SEND_PARTIAL_ADDRESS_FAILURE
        MailSender failingSender = senderThatRejectsRecipient("bad-user@internal.domain");
        failingSender.setSendPartial(true);
        AutoSendDraftTask task = taskWithSender(mbox.getId(), draftId, failingSender);

        // Act
        task.call();

        // Assert
        Message draftAfter = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
        assertNotNull(draftAfter, "Draft should still exist in the mailbox");
        assertEquals(0L, draftAfter.getDraftAutoSendTime(),
                "autoSendTime must be cleared to 0 to prevent restart-loop re-scheduling");
    }
}

