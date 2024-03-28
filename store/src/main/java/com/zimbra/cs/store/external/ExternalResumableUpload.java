// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import java.io.IOException;

import com.zimbra.common.service.ServiceException;

public interface ExternalResumableUpload {

    /**
     * Create a new ExternalResumableIncomingBlob instance to handle the upload
     * of a single object. The implementation should compute all remote metadata
     * such as remote id, size, and content hash inline with the upload process
     * so that finishUpload() does not need to traverse the data again
     *
     * @param id: local upload ID. Used internally; must be passed to super constructor
     * @param ctxt: local upload context. Used internally; must be passed to super constructor
     * @return initialized ExternalResumableIncomingBlob instance ready to accept a new data upload
     * @throws IOException
     * @throws ServiceException
     */
    ExternalResumableIncomingBlob newIncomingBlob(String id, Object ctxt) throws IOException, ServiceException;

    /**
     * Finalize an upload. Depending on store semantics this may involve a
     * commit, checksum, or other similar operation.
     *
     * @param blob: The ExternalUploadedBlob which data has been written into
     * @return String identifier (locator) for the permanent storage location for the uploaded content
     * @throws IOException
     * @throws ServiceException
     */
    String finishUpload(ExternalUploadedBlob blob) throws IOException, ServiceException;
}
