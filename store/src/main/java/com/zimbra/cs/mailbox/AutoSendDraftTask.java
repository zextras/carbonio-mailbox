// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.util.ItemId;

import java.util.Date;
import java.util.Set;

/**
 * Auto-send-draft scheduled task.
 *
 * <p>When the scheduled send-later time arrives this task:
 * <ol>
 *   <li>Attempts to send the draft using <em>partial-send</em> mode so that valid recipients
 *       still receive the message even when one or more recipients are rejected by the MTA.</li>
 *   <li>On a <strong>permanent</strong> failure:
 *       <ul>
 *         <li>{@code SEND_PARTIAL_ADDRESS_FAILURE} — some valid recipients already received the
 *             message; the draft is <strong>deleted</strong> to avoid confusion and prevent
 *             accidental duplicate sends.</li>
 *         <li>{@code SEND_ABORTED_ADDRESS_FAILURE} or {@code MESSAGE_TOO_BIG} — nothing was
 *             delivered; the draft is <strong>kept</strong> so the user can correct it, but
 *             {@code autoSendTime} is cleared to {@code 0} so the task is never rescheduled and
 *             the retry loop stops.</li>
 *       </ul>
 *   </li>
 *   <li>Transient / connectivity failures are still propagated so the task scheduler can
 *       retry them normally.</li>
 * </ol>
 */
public class AutoSendDraftTask extends ScheduledTask<Object> {

    private static final String TASK_NAME_PREFIX = "autoSendDraftTask";
    private static final String DRAFT_ID_PROP_NAME = "draftId";

    /**
     * {@link MailServiceException} error codes that represent permanent, non-retriable send
     * failures. Retrying these would always produce the same result and — in the case of the
     * SendLater feature — cause an infinite retry loop.
     *
     * <ul>
     *   <li>{@code SEND_ABORTED_ADDRESS_FAILURE} — one or more RCPT commands were rejected by the
     *       MTA with a 5xx permanent error and {@code sendPartial=false}: nothing was delivered.
     *   <li>{@code SEND_PARTIAL_ADDRESS_FAILURE} — same rejection but {@code sendPartial=true}:
     *       valid recipients were delivered, invalid ones were not.
     *   <li>{@code MESSAGE_TOO_BIG} — the message exceeds the server's
     *       {@code zimbraMtaMaxMessageSize} limit. The message content cannot change between
     *       retries, so retrying is pointless.
     * </ul>
     */
    private static final Set<String> PERMANENT_SEND_FAILURE_CODES = Set.of(
        MailServiceException.SEND_ABORTED_ADDRESS_FAILURE,
        MailServiceException.SEND_PARTIAL_ADDRESS_FAILURE,
        MailServiceException.MESSAGE_TOO_BIG
    );

    /**
     * Returns the task name.
     */
    @Override
    public String getName() {
        return TASK_NAME_PREFIX + getProperty(DRAFT_ID_PROP_NAME);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Void call() throws Exception {
        ZimbraLog.scheduler.debug("Running task %s", this);
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        if (mbox == null) {
            ZimbraLog.scheduler.error("Mailbox for id %s does not exist", getMailboxId());
            return null;
        }
        Integer draftId = Integer.valueOf(getProperty(DRAFT_ID_PROP_NAME));
        Message msg;
        try {
            msg = (Message) mbox.getItemById(null, draftId, MailItem.Type.MESSAGE);
        } catch (MailServiceException.NoSuchItemException e) {
            // Draft might have been deleted
            ZimbraLog.scheduler.debug("Draft message with id %s no longer exists in mailbox", draftId);
            return null;
        }
        if (msg.getDraftAutoSendTime() == 0) {
            ZimbraLog.scheduler.warn("Message with id %s is not a Draft scheduled to be auto-sent", draftId);
            return null;
        }
        if (msg.isTagged(Flag.FlagInfo.DELETED) || msg.inTrash()) {
            ZimbraLog.scheduler.debug("Draft with id %s is deleted", draftId);
            return null;
        }

        // Send the draft.
        // Use sendPartial=true so that the message is delivered to all valid recipients even
        // when one or more RCPT commands are rejected by the MTA (5xx permanent failure).
        MailSender mailSender = getMailSender(mbox);
        mailSender.setSendPartial(true);
        mailSender.setOriginalMessageId(StringUtil.isNullOrEmpty(msg.getDraftOrigId()) ? null
                : new ItemId(msg.getDraftOrigId(), mbox.getAccountId()));
        mailSender.setReplyType(StringUtil.isNullOrEmpty(msg.getDraftReplyType()) ? null : msg.getDraftReplyType());
        mailSender.setIdentity(StringUtil.isNullOrEmpty(msg.getDraftIdentityId()) ? null
                : mbox.getAccount().getIdentityById(msg.getDraftIdentityId()));

        try {
            mailSender.sendMimeMessage(new OperationContext(mbox), mbox, msg.getMimeMessage());
        } catch (MailServiceException e) {
            if (PERMANENT_SEND_FAILURE_CODES.contains(e.getCode())) {
                // Permanent failure – stop retrying.
                ZimbraLog.scheduler.info(
                        "Permanent send failure [%s] while auto-sending draft id=%s; Error: %s",
                        e.getCode(), draftId, e.getMessage());

                if (MailServiceException.SEND_PARTIAL_ADDRESS_FAILURE.equals(e.getCode())) {
                    // Valid recipients already received the message; delete the draft to prevent
                    // the user from accidentally re-sending and creating duplicates.
                    ZimbraLog.scheduler.info(
                            "Deleting draft id=%s after partial send (valid recipients already received it).",
                            draftId);
                    mbox.delete(null, draftId, MailItem.Type.MESSAGE);
                } else {
                    // Nothing was delivered (SEND_ABORTED_ADDRESS_FAILURE / MESSAGE_TOO_BIG);
                    // keep the draft so the user can correct it, but clear autoSendTime so it
                    // is not rescheduled and the retry loop does not continue.
                    ZimbraLog.scheduler.info(
                            "Clearing autoSendTime on draft id=%s after undelivered permanent failure.",
                            draftId);
                    boolean success = false;
                    try {
                        mbox.beginTransaction("clearDraftAutoSendTime", null);
                        msg.setDraftAutoSendTime(0);
                        success = true;
                    } finally {
                        mbox.endTransaction(success);
                    }
                }
                return null;
            }
            // All other exceptions (transient failures, etc.) are re-thrown so the
            // scheduler can decide whether to retry.
            throw e;
        }

        // now delete the draft
        mbox.delete(null, draftId, MailItem.Type.MESSAGE);
        return null;
    }

    /**
     * Returns the {@link MailSender} to use for sending the draft. Exposed as {@code protected}
     * so that tests can override it without requiring a full {@link Mailbox} or
     * {@link com.zimbra.cs.mailbox.MailboxManager} spy.
     */
    protected MailSender getMailSender(Mailbox mbox) throws ServiceException {
        return mbox.getMailSender();
    }


    /**
     * Cancels any existing scheduled auto-send task for the given draft.
     *
     * @param draftId the draft message ID
     * @param mailboxId the mailbox ID
     * @throws ServiceException if cancellation fails
     */
    public static void cancelTask(int draftId, int mailboxId) throws ServiceException {
        ScheduledTaskManager.cancel(AutoSendDraftTask.class.getName(),
                TASK_NAME_PREFIX + draftId,
                mailboxId,
                true);

    }

    /**
     * Schedules an auto-send task for the given draft at the specified time.
     *
     * @param draftId the draft message ID
     * @param mailboxId the mailbox ID
     * @param autoSendTime the timestamp when the message should be sent
     * @throws ServiceException if scheduling fails
     */
    public static void scheduleTask(int draftId, int mailboxId, long autoSendTime) throws ServiceException {
        AutoSendDraftTask autoSendDraftTask = new AutoSendDraftTask();
        autoSendDraftTask.setMailboxId(mailboxId);
        autoSendDraftTask.setExecTime(new Date(autoSendTime));
        autoSendDraftTask.setProperty(DRAFT_ID_PROP_NAME, Integer.toString(draftId));
        ScheduledTaskManager.schedule(autoSendDraftTask);
    }
}
