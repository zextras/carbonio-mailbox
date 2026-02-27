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
 */
public class AutoSendDraftTask extends ScheduledTask<Object> {

    private static final String TASK_NAME_PREFIX = "autoSendDraftTask";
    private static final String DRAFT_ID_PROP_NAME = "draftId";

    /**
     * MailServiceException error codes that represent permanent, non-retriable send failures
     * caused by invalid recipient addresses. Retrying these would only produce the same result
     * and — in the case of the SendLater feature — cause an infinite retry loop..sq
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
        // send draft
        MailSender mailSender = getMailSender(mbox);
        mailSender.setOriginalMessageId(StringUtil.isNullOrEmpty(msg.getDraftOrigId()) ? null
                : new ItemId(msg.getDraftOrigId(), mbox.getAccountId()));
        mailSender.setReplyType(StringUtil.isNullOrEmpty(msg.getDraftReplyType()) ? null : msg.getDraftReplyType());
        mailSender.setIdentity(StringUtil.isNullOrEmpty(msg.getDraftIdentityId()) ? null
                : mbox.getAccount().getIdentityById(msg.getDraftIdentityId()));
        try {
            mailSender.sendMimeMessage(new OperationContext(mbox), mbox, msg.getMimeMessage());
        } catch (MailServiceException e) {
            if (PERMANENT_ADDRESS_FAILURE_CODES.contains(e.getCode())) {
                // Permanent failure due to invalid recipient address(es). Retrying will never
                // succeed, so clear the scheduled send time to prevent infinite retry loops
                // (both in-memory via TaskScheduler and across restarts via the DB task entry).
                ZimbraLog.scheduler.warn(
                        "Scheduled send failed permanently for draft id=%d due to invalid"
                                + " recipient(s); clearing autoSendTime: %s",
                        draftId, e.getMessage());
                boolean success = false;
                try {
                    mbox.beginTransaction("clearDraftAutoSendTime", null);
                    msg.setDraftAutoSendTime(0);
                    success = true;
                } finally {
                    mbox.endTransaction(success);
                }
                return null;
            }
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
     * @param draftId
     * @param mailboxId
     * @throws ServiceException
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
     * @param draftId
     * @param mailboxId
     * @param autoSendTime
     * @throws ServiceException
     */
    public static void scheduleTask(int draftId, int mailboxId, long autoSendTime) throws ServiceException {
        AutoSendDraftTask autoSendDraftTask = new AutoSendDraftTask();
        autoSendDraftTask.setMailboxId(mailboxId);
        autoSendDraftTask.setExecTime(new Date(autoSendTime));
        autoSendDraftTask.setProperty(DRAFT_ID_PROP_NAME, Integer.toString(draftId));
        ScheduledTaskManager.schedule(autoSendDraftTask);
    }
}
