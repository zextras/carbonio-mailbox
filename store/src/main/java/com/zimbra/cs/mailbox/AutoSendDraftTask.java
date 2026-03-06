// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.service.ServiceException.Argument;
import com.zimbra.common.util.MailUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.cs.util.JMSession;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Auto-send-draft scheduled task.
 *
 * <p>When the scheduled send-later time arrives this task:
 * <ol>
 *   <li>Attempts to send the draft using <em>partial-send</em> mode so that valid recipients
 *       still receive the message even when one or more recipients are rejected by the MTA.</li>
 *   <li>On a <strong>permanent</strong> address failure ({@code SEND_ABORTED_ADDRESS_FAILURE} or
 *       {@code SEND_PARTIAL_ADDRESS_FAILURE}):
 *       <ul>
 *         <li>Clears the {@code autoSendTime} metadata on the draft so the task is not
 *             rescheduled and the retry loop stops.</li>
 *         <li>Sends an NDR (Non-Delivery Report) back to the sender listing the addresses
 *             that could not be reached.</li>
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
     * MailServiceException error codes that represent permanent, non-retriable send failures
     * caused by invalid recipient addresses. Retrying these would only produce the same result
     * and — in the case of the SendLater feature — cause an infinite retry loop.
     */
    private static final Set<String> PERMANENT_ADDRESS_FAILURE_CODES = Set.of(
        MailServiceException.SEND_ABORTED_ADDRESS_FAILURE,
        MailServiceException.SEND_PARTIAL_ADDRESS_FAILURE
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
            if (PERMANENT_ADDRESS_FAILURE_CODES.contains(e.getCode())) {
                // Permanent recipient failure – stop retrying and notify the sender.
                ZimbraLog.scheduler.warn(
                        "Permanent address failure while auto-sending draft id=%s; "
                                + "clearing autoSendTime and sending NDR to sender. Error: %s",
                        draftId, e.getMessage());
                // Clear the scheduled-send time so the task is never rescheduled.
                boolean success = false;
                try {
                    mbox.beginTransaction("clearDraftAutoSendTime", null);
                    msg.setDraftAutoSendTime(0);
                    success = true;
                } finally {
                    mbox.endTransaction(success);
                }
                // Attempt to send an NDR so the user knows what happened.
                sendNdr(mbox, msg, e.getArgs());
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
     * Sends a Non-Delivery Report to the mailbox owner (the original sender of the draft)
     * listing every address that could not be delivered.
     *
     * @param mbox      owner mailbox
     * @param draft     the draft message that failed to send
     * @param addrArgs  the {@link Argument} list carried by the {@link MailServiceException}
     *                  (contains "invalid" and/or "unsent" address entries)
     */
    private void sendNdr(Mailbox mbox, Message draft, List<Argument> addrArgs) {
        try {
            Account account = mbox.getAccount();
            InternetAddress senderAddr = AccountUtil.getFriendlyEmailAddress(account);
            String senderEmail = account.getName();

            MimeMessage ndr = new MimeMessage(JMSession.getSmtpSession(account));
            String subject;
            try {
                subject = draft.getMimeMessage().getSubject();
            } catch (MessagingException ex) {
                subject = "(no subject)";
            }
            MailUtil.populateFailureDeliveryMessageFields(ndr, subject, senderEmail, addrArgs, senderAddr);

            MailSender ndrSender = getMailSender(mbox);
            ndrSender.setSaveToSent(false);
            ndrSender.setEnvelopeFrom("<>");
            ndrSender.sendMimeMessage(new OperationContext(mbox), mbox, ndr);
        } catch (Exception ex) {
            ZimbraLog.scheduler.warn(
                    "Failed to send NDR for auto-send draft id=%s: %s", draft.getId(), ex.getMessage(), ex);
        }
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
