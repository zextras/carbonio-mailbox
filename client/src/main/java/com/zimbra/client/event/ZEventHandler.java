// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;
import com.zimbra.soap.type.AccountWithModifications;
import com.zimbra.client.ZMailbox;
import com.zimbra.common.service.ServiceException;

public class ZEventHandler {

    /**
     * default implementation is a no-op.
     * 
     * @param refreshEvent the refresh event
     * @param mailbox the mailbox that had the event
     */
    public void handleRefresh(ZRefreshEvent refreshEvent, ZMailbox mailbox) throws ServiceException {
        // do nothing by default
    }

    /**
     *
     * default implementation is a no-op
     *
     * @param event the create event
     * @param mailbox the mailbox that had the event
     */
    public void handleCreate(ZCreateEvent event, ZMailbox mailbox) throws ServiceException {
        // do nothing by default
    }

    /**
     *
     * default implementation is a no-op
     *
     * @param event the modify event
     * @param mailbox the mailbox that had the event
     */
    public void handleModify(ZModifyEvent event, ZMailbox mailbox) throws ServiceException {
        // do nothing by default
    }

     /**
     *
     * default implementation is a no-op
     *
     * @param event the delete event
     * @param mailbox the mailbox that had the event
     */
    public void handleDelete(ZDeleteEvent event, ZMailbox mailbox) throws ServiceException {
        // do nothing by default
    }

    /**
    *
    * default implementation is a no-op
    *
    * @param mods JAXB class with pending modifications
    */
    public void handlePendingModification(int changeId, AccountWithModifications info) throws ServiceException {
       // do nothing by default
   }
}
