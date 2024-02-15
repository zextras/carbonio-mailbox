// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import java.io.IOException;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.StagedBlob;

public class VolumeStagedBlob extends StagedBlob {
    private final VolumeBlob mLocalBlob;
    private boolean mWasStagedDirectly;

    public VolumeStagedBlob(Mailbox mbox, VolumeBlob blob) throws IOException {
        super(mbox, blob.getDigest(), blob.getRawSize());
        mLocalBlob = blob;
    }

    public VolumeBlob getLocalBlob() {
        return mLocalBlob;
    }

    @Override public String getLocator() {
        return Short.toString(mLocalBlob.getVolumeId());
    }

    VolumeStagedBlob markStagedDirectly() {
        mWasStagedDirectly = true;
        return this;
    }

    boolean wasStagedDirectly() {
        return mWasStagedDirectly;
    }
}
