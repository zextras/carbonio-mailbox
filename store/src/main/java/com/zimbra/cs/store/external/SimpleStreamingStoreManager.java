// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;

/**
 * Example implementation of ExternalResumableUpload which writes to a flat directory structure
 * This is intended for illustration purposes only; it should *never* be used in a production environment
 *
 */
public class SimpleStreamingStoreManager extends SimpleStoreManager implements ExternalResumableUpload {

    String uploadDirectory = "/tmp/simplestore/uploads";

    @Override
    public void startup() throws IOException, ServiceException {
        super.startup();
        FileUtil.mkdirs(new File(uploadDirectory));
    }

    @Override
    public String finishUpload(ExternalUploadedBlob blob) throws IOException,
                    ServiceException {
        ZimbraLog.store.info("finishing upload to "+blob.getUploadId());
        return blob.getUploadId();
    }

    @Override
    public ExternalResumableIncomingBlob newIncomingBlob(String id, Object ctxt) throws IOException, ServiceException {
        return new SimpleStreamingIncomingBlob(id, getBlobBuilder(), ctxt);
    }

    private class SimpleStreamingIncomingBlob extends ExternalResumableIncomingBlob {

        private final File file;

        public SimpleStreamingIncomingBlob(String id, BlobBuilder blobBuilder,
                        Object ctx) throws ServiceException, IOException {
            super(id, blobBuilder, ctx);
            String baseName = uploadDirectory+"/upload-"+id;
            String name = baseName;

            synchronized (this) {
                int count = 1;
                File upFile = new File(name+".upl");
                while (upFile.exists()) {
                    name = baseName+"_"+count++;
                    upFile = new File(name+".upl");
                }
                if (upFile.createNewFile()) {
                    ZimbraLog.store.debug("writing to new file %s",upFile.getName());
                    file = upFile;
                } else {
                    throw new IOException("unable to create new file");
                }
            }
        }

        @Override
        protected ExternalResumableOutputStream getAppendingOutputStream(BlobBuilder blobBuilder) throws IOException {
            return new SimpleStreamingOutputStream(blobBuilder, file);
        }

        @Override
        protected long getRemoteSize() throws IOException {
            return file.length();
        }

        @Override
        public Blob getBlob() throws IOException, ServiceException {
            return new ExternalUploadedBlob(blobBuilder.finish(), file.getCanonicalPath());
        }
    }

    private class SimpleStreamingOutputStream extends ExternalResumableOutputStream {

        private final FileOutputStream fos;

        public SimpleStreamingOutputStream(BlobBuilder blobBuilder, File file) throws IOException {
            super(blobBuilder);
            this.fos = new FileOutputStream(file, true);
        }

        @Override
        protected void writeToExternal(byte[] b, int off, int len)
                        throws IOException {
            fos.write(b, off, len);
        }
    }
}
