// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import java.io.IOException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.volume.VolumeManager;

public final class VolumeBlobBuilder extends BlobBuilder {

    VolumeBlobBuilder(Blob targetBlob) {
        super(targetBlob);
    }

    private short getVolumeId() {
        return ((VolumeBlob) blob).getVolumeId();
    }

    @Override
    protected boolean useCompression() throws IOException {
        if (disableCompression) {
            return false;
        }
        try {
            return VolumeManager.getInstance().getVolume(getVolumeId()).isCompressBlobs();
        } catch (ServiceException e) {
            throw new IOException("Unable to determine volume compression flag", e);
        }
    }


    @Override
    protected int getCompressionThreshold() {
        try {
            return (int) VolumeManager.getInstance().getVolume(getVolumeId()).getCompressionThreshold();
        } catch (ServiceException e) {
            ZimbraLog.store.error("Unable to determine volume compression threshold", e);
        }
        return 0;
    }

    @Override
    public Blob finish() throws IOException, ServiceException {
        if (isFinished()) {
            return blob;
        }
        super.finish();
        return blob;
    }

    @Override
    public String toString() {
        return super.toString() + ", volume=" + getVolumeId();
    }
}
