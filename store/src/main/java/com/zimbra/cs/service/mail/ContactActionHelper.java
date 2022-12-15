// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.ArrayList;
import java.util.List;

import com.zimbra.common.mailbox.Color;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;

public class ContactActionHelper extends ItemActionHelper {

    public static ContactActionHelper UPDATE(ZimbraSoapContext zsc, OperationContext octxt,
            Mailbox mbox, List<Integer> ids, ItemId iidFolder,
            String flags, String[] tags, Color color, ParsedContact pc)
    throws ServiceException {
        ContactActionHelper ca = new ContactActionHelper(octxt, mbox, zsc.getResponseProtocol(), ids, Op.UPDATE);
        ca.setIidFolder(iidFolder);
        ca.setFlags(flags);
        ca.setTags(tags);
        ca.setColor(color);
        ca.setParsedContact(pc);
        ca.schedule();
        return ca;
    }

    // only when OP=UPDATE
    private ParsedContact mParsedContact;


    public void setParsedContact(ParsedContact pc) {
        assert(mOperation == Op.UPDATE);
        mParsedContact = pc;
    }

    ContactActionHelper(OperationContext octxt, Mailbox mbox, SoapProtocol responseProto, List<Integer> ids, Op op) throws ServiceException {
        super(octxt, mbox, responseProto, ids, op, MailItem.Type.CONTACT, true, null);
    }

    @Override
    protected void schedule() throws ServiceException {
        // iterate over the local items and perform the requested operation
        switch (mOperation) {
        case UPDATE:
            if (!mIidFolder.belongsTo(getMailbox())) {
                throw ServiceException.INVALID_REQUEST("cannot move item between mailboxes", null);
            }

            if (mIidFolder.getId() > 0) {
                getMailbox().move(getOpCtxt(), itemIds, type, mIidFolder.getId(), mTargetConstraint);
            }
            if (mTags != null || mFlags != null) {
                getMailbox().setTags(getOpCtxt(), itemIds, type, Flag.toBitmask(mFlags), mTags, mTargetConstraint);
            }
            if (mColor != null) {
                getMailbox().setColor(getOpCtxt(), itemIds, type, mColor);
            }
            if (mParsedContact != null) {
                for (int id : itemIds) {
                    getMailbox().modifyContact(getOpCtxt(), id, mParsedContact);
                }
            }
            break;
        default:
            throw ServiceException.INVALID_REQUEST("unknown operation: " + mOperation, null);
        }

        List<String> successes = new ArrayList<String>();
        for (int id : itemIds) {
            successes.add(mIdFormatter.formatItemId(id));
        }
        mResult = ItemActionResult.create(mOperation);
        mResult.appendSuccessIds(successes);
    }

    @Override
    public String toString() {
        StringBuilder toRet = new StringBuilder(super.toString());
        if (mOperation == Op.UPDATE) {
            if (mParsedContact != null) {
                toRet.append(" Fields=").append(mParsedContact.getFields());
            }
        }
        return toRet.toString();
    }
}
