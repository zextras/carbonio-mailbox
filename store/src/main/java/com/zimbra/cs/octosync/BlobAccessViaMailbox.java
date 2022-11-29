// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.octosync;

import java.io.InputStream;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Document;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;

/**
 * Implements BlobIndex. Retrieves files (blobs) via supplied Mailbox.
 */
public class BlobAccessViaMailbox implements BlobAccess
{
    /** Mailbox to get blobs from */
    private Mailbox mbox;
    /** Associated operation context */
    private OperationContext opContext;
    /** Default file id */
    private int defaultFileId;
    /** Default version */
    private int defaultVersion;

    /**
     * Instantiates a new mailbox blob index.
     *
     * @param mbox The mailbox to get blobs from
     * @param opContext The operation context
     * @param defaultFileId Default file id; 0 passed to getBlobInputStream() will be
     *     mapped to this id. This is support default references in the patch
     *     (referring to the latest revision of the file being uploaded)
     * @param defaultVersion Default version number; 0 passed to getBlobInputStream() will be mapped to this numbeer
     */
    public BlobAccessViaMailbox(Mailbox mbox, OperationContext opContext, int defaultFileId, int defaultVersion)
    {
        this.mbox = mbox;
        this.opContext = opContext;
        this.defaultFileId = defaultFileId;
        this.defaultVersion = defaultVersion;
    }

    // BlobAccess API
    @Override
    public int[] getActualReference(int fileId, int version)
    {
        int[] result = new int[2];

        if (fileId == 0 && version == 0) {
            result[0] = defaultFileId;
            result[1] = defaultVersion;
        } else {
            result[0] = fileId;
            result[1] = version;
        }

        return result;
    }

    // BlobAccess API
    @Override
    public InputStream getBlobInputStream(int fileId, int version)
        throws ServiceException, InvalidPatchReferenceException
    {
        if (fileId == 0 && version == 0) {
            fileId = defaultFileId;
            version = defaultVersion;
        }

        try {
            Document doc = (Document)mbox.getItemRevision(opContext, fileId, MailItem.Type.DOCUMENT, version);

            // note, getItemRevision is inconsistent about error reporting
            // throws if the document (item) is not found, but returns null
            // if version was not found

            if (doc == null) {
                throw new InvalidPatchReferenceException("Reference to non-existing version " + version +
                        " of document " + fileId);
            }

            return doc.getContentStream();

        } catch (NoSuchItemException e) {
            throw new InvalidPatchReferenceException("Reference to non-existing document: " + fileId
                    + " (version: " + version + ")", e);
        }
    }
}
