// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.octosync.store;

import java.io.IOException;
import java.io.InputStream;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.IncomingBlob;


/**
 * Abstract interface representing generic storage of blobs.
 *
 * This interface is currently used for storing Octopus patches only. It provides
 * simplest layer of abstraction with the functionality required for storing, retrieving and
 * deleting patch files. It is to be implemented using existing blob storage
 * facilities such as StoreManager. In the future, as we modernize the storage back-end
 * this API may evolve into widely used general purpose API. For the time being, it
 * just remains the glue between patch storage and the existing storage facilities.
 *
 * The key idea in this API is that incoming blobs are distinguished from stored blobs.
 * Incoming blob is a blob that is being received, and is possibly not yet complete.
 * It can be appended to as the data is being received. This supports resumable uploads.
 * Once complete, it can be turned into stored blob. An incoming blob that never got stored
 * SHOULD be automatically purged. A stored blob MAY be purged; at this time the only way to
 * ensure blob persistence is to associate it with a Document or other MailItem. Future BlobStore
 * implementations MAY automatically persist StoredBlob metadata, but no such implementation currently
 * exists.
 *
 * Incoming blobs are assigned ids by the store. Stored blobs use user supplied ids.
 * Namespaces for both types are separate. Additionally stored blobs support versioning.
 *
 * @author grzes
 *
 */
public abstract class BlobStore
{
    /**
     * Represents blob that was stored after all data
     * for the incoming blob were received.
     *
     * The details of how the blob is stored, possible aspects such as
     * retention period are implementation dependent.
     */
    public abstract class StoredBlob
    {
        private String id;
        protected Blob blob;

        /**
         * Gets the stored blob identifier.
         *
         * @return the id
         */
        public String getId()
        {
            return id;
        }

        /**
         * Gets the local blob instance
         * @return the blob
         */
        public Blob getBlob() {
            return blob;
        }

        /**
         * Gets the input stream.
         *
         * @return the input stream
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public abstract InputStream getInputStream() throws IOException;

        /**
         * Gets the user context. If the context was set for the
         * incoming blob used to create stored blob, the same object must be
         * preserved and returned here.
         *
         * @return the context
         */
        public abstract Object getContext();

        /**
         * Sets the user context.
         *
         * @param value the new context
         */
        public abstract void setContext(Object value);

        /**
         * Returns the patch size (in bytes). This must be actual
         * patch size, uncompressed.
         *
         * @return the patch size
         */
        public abstract long getSize();

        protected StoredBlob(String id, Blob blob)
        {
            this.id = id;
            this.blob = blob;
        }
    }

    /**
     * Creates an incoming blob.
     *
     * @param ctx User context. Can be null.
     *
     * @return Instance of IncomingBlob
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServiceException the service exception
     */
    public abstract IncomingBlob createIncoming(Object ctx) throws IOException, ServiceException;

    /**
     * Retrieves the incoming blob by the id.
     *
     * @param id The id passed to createIncoming() when the blob was created.
     *
     * @return The IncomingBlob instance or null if not found.
     */
    public abstract IncomingBlob getIncoming(String id);

    /**
     * Rejects incoming blob. Called when an incoming blob cannot be accepted, e.g.
     * if complete data were never received.
     *
     * @param ib The IncomingBlob instance
     */
    public abstract void deleteIncoming(IncomingBlob ib);

    /**
     * Stores an incoming blob.
     *
     * @param ib The IncomingBlob instance to accept.
     * @param id The id for the stored blob.
     * @param version the version The version of the blob. Must be 1 or greater. Must
     *      not be equal to a version that already exists.
     *
     * @return StoredBlob instance
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServiceException
     */
    public abstract StoredBlob store(IncomingBlob ib, String id, int version) throws IOException, ServiceException;

    /**
     * Gets the specified version of a StoredBlob.
     *
     * @param id Stored blob identifier.
     * @param version the version
     * @return the stored blob
     */
    public abstract StoredBlob get(String id, int version);

    /**
     * Retrieves the latest version of a StoredBlob.
     *
     * @param id Stored blob identifier.
     * @return the stored blob
     */
    public abstract StoredBlob get(String id);

    /**
     * Delete all version of a specified stored blob.
     *
     * @param sb Stored blob to delete. Can be null in which case it is no-op.
     */
    public abstract void delete(StoredBlob sb);

    /**
     * Delete specific version of a stored blob.
     *
     * @param sb Stored blob to delete. Can be null in which case it is no-op.
     * @param version The version to delete. No-op if the version does not exist.
     */
    public abstract void delete(StoredBlob sb, int version);

    /**
     * Determine if the store supports SHA-256 SIS
     *
     * @return true if SIS is supported, false if not
     */
    public abstract boolean supportsSisCreate();

    /**
     * Retrieve a previously uploaded blob based on hash. The implementation must track reference count and increment if a blob is found.
     * @param hash SHA256 hash of the blob
     * @return StoredBlob which matches hash, or null if no such blob exists
     * @throws IOException
     * @throws ServiceException
     */
    public abstract StoredBlob getSisBlob(byte[] hash) throws IOException, ServiceException;
}
