// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.external.ExternalBlobConsistencyChecker;
import com.zimbra.cs.store.external.ExternalStoreManager;
import com.zimbra.cs.store.file.BlobConsistencyChecker;
import com.zimbra.cs.store.file.FileBlobStore;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import com.zimbra.soap.ZimbraSoapContext;

public final class CheckBlobConsistency extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        checkRight(zsc, context, null, AdminRight.PR_SYSTEM_ADMIN_ONLY);


        // Assemble the list of mailboxes.
        List<Integer> mailboxIds = new ArrayList<Integer>();
        List<Element> mboxElementList = request.listElements(AdminConstants.E_MAILBOX);
        if (mboxElementList.isEmpty()) {
            // Get all mailbox id's.
            for (int mboxId : MailboxManager.getInstance().getMailboxIds()) {
                mailboxIds.add(mboxId);
            }
        } else {
            // Read mailbox id's from the request.
            for (Element mboxEl : mboxElementList) {
                Mailbox mbox = MailboxManager.getInstance().getMailboxById((int) mboxEl.getAttributeLong(AdminConstants.A_ID));
                mailboxIds.add(mbox.getId());
            }
        }

        boolean checkSize = request.getAttributeBool(AdminConstants.A_CHECK_SIZE, true);
        boolean reportUsedBlobs = request.getAttributeBool(AdminConstants.A_REPORT_USED_BLOBS, false);

        // Check blobs and assemble response.
        Element response = zsc.createElement(AdminConstants.CHECK_BLOB_CONSISTENCY_RESPONSE);

        StoreManager sm = StoreManager.getInstance();
        if (sm instanceof ExternalStoreManager) {

            for (int mboxId : mailboxIds) {
                ExternalBlobConsistencyChecker checker = new ExternalBlobConsistencyChecker();
                BlobConsistencyChecker.Results results = checker.check(null, mboxId, checkSize, reportUsedBlobs);
                if (results.hasInconsistency() || reportUsedBlobs) { //or checking used blobs
                    Element mboxEl = response.addElement(AdminConstants.E_MAILBOX).addAttribute(AdminConstants.A_ID, mboxId);
                    results.toElement(mboxEl);
                }
            }
        } else if (sm instanceof FileBlobStore) {

            // Assemble the list of volumes.
            List<Short> volumeIds = new ArrayList<Short>();
            List<Element> volumeElementList = request.listElements(AdminConstants.E_VOLUME);
            if (volumeElementList.isEmpty()) {
                // Get all message volume id's.
                for (Volume vol : VolumeManager.getInstance().getAllVolumes()) {
                    switch (vol.getType()) {
                        case Volume.TYPE_MESSAGE:
                        case Volume.TYPE_MESSAGE_SECONDARY:
                            volumeIds.add(vol.getId());
                            break;
                    }
                }
            } else {
                // Read volume id's from the request.
                for (Element volumeEl : volumeElementList) {
                    short volumeId = (short) volumeEl.getAttributeLong(AdminConstants.A_ID);
                    Volume vol = VolumeManager.getInstance().getVolume(volumeId);
                    if (vol.getType() == Volume.TYPE_INDEX) {
                        throw ServiceException.INVALID_REQUEST("Index volume " + volumeId + " is not supported", null);
                    } else {
                        volumeIds.add(volumeId);
                    }
                }
            }

            for (int mboxId : mailboxIds) {
                BlobConsistencyChecker checker = new BlobConsistencyChecker();
                BlobConsistencyChecker.Results results = checker.check(volumeIds, mboxId, checkSize, reportUsedBlobs);
                if (results.hasInconsistency() || reportUsedBlobs) {
                    Element mboxEl = response.addElement(AdminConstants.E_MAILBOX).addAttribute(AdminConstants.A_ID, mboxId);
                    results.toElement(mboxEl);
                }
            }
        } else {
            //neither ExternalStoreManager nor FileBlobStore..some third type we haven't coded for
            throw ServiceException.INVALID_REQUEST(sm.getClass().getName() + " is not supported", null);
        }
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(AdminRightCheckPoint.Notes.SYSTEM_ADMINS_ONLY);
    }
}
