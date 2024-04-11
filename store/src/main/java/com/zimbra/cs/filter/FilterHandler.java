// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.filter.jsieve.ActionFlag;
import com.zimbra.cs.filter.jsieve.ErejectException;
import com.zimbra.cs.lmtpserver.LmtpEnvelope;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;

/**
 * Interface implemented by classes that handle filter rule actions.
 */
interface FilterHandler {

    Message getMessage() throws ServiceException;

    ParsedMessage getParsedMessage() throws ServiceException;

    MimeMessage getMimeMessage() throws ServiceException;

    public default DeliveryContext getDeliveryContext() {
        return null;
    }

    int getMessageSize();

    /**
     * Returns the path to the default folder (usually <tt>Inbox</tt>).
     */
    String getDefaultFolderPath() throws ServiceException;

    /**
     * Executed before mail filtering begins.
     */
    void beforeFiltering() throws ServiceException;

    /**
     * Discards the message.  This method will only be called when there
     * are no <tt>fileinto</tt> or <tt>keep</tt> actions.
     */
    void discard() throws ServiceException;

    /**
     * Files the message into either the default folder or the
     * spam folder.  This method will not be called multiple times
     * for the same message.
     * @return the new message, or <tt>null</tt> if it was a duplicate
     */
    Message implicitKeep(Collection<ActionFlag> flagActions, String[] tags) throws ServiceException;

    /**
     * Files the message into the default folder without taking
     * spam filtering into account.  This method will not be called multiple times
     * for the same message.
     * @return the new message, or <tt>null</tt> if it was a duplicate
     */
    Message explicitKeep(Collection<ActionFlag> flagActions, String[] tags) throws ServiceException;

    /**
     * Files the message into the given folder.  May be local or remote.
     * This method will not be called multiple times for the same path.
     * @return the new message, or <tt>null</tt> if it was a duplicate
     */
    ItemId fileInto(String folderPath, Collection<ActionFlag> flagActions, String[] tags) throws ServiceException;

    /**
     * Redirects the message to another address.
     */
    void redirect(String destinationAddress) throws ServiceException;

    /**
     * Replies to the message.
     */
    void reply(String bodyTemplate) throws ServiceException, MessagingException;

    /**
     * Executed after mail filtering ends.
     */
    void afterFiltering() throws ServiceException;

    /**
     * Sends an email notification.
     */
    void notify(String emailAddr, String subjectTemplate, String bodyTemplate, int maxBodyBytes,
        List<String> origHeaders) throws ServiceException, MessagingException;

    /**
     * Rejects delivery of a message.
     */
    void reject(String reason, LmtpEnvelope envelope) throws ServiceException, MessagingException;

    /**
     * Execute erejects action.
     */
    void ereject(LmtpEnvelope envelope) throws ErejectException;

    /**
     * Sends an email notification (RFC 5435 and 5436 compliant)
     * @param from From address specified by :from tag
     * @param importance Importance integer specified by :importance tag
     * @param options Option string list specified by :options tag
     * @param message Subject string specified by :message tag
     * @param mailto To address specified in the method parameter
     * @param mailtoParams Set of key and value specified in the method parameter
     * @throws ServiceException
     * @throws MessagingException
     */
    void notifyMailto(LmtpEnvelope envelope, String from, int importance,
        Map<String, String> options, String message, String mailto,
        Map<String, List<String>> mailtoParams) throws ServiceException, MessagingException;
}
