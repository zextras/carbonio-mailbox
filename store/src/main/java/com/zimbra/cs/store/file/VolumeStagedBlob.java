// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.StagedBlob;
import java.io.IOException;

public class VolumeStagedBlob extends StagedBlob {
  private VolumeBlob mLocalBlob;
  private boolean mWasStagedDirectly;

  VolumeStagedBlob(Mailbox mbox, VolumeBlob blob) throws IOException {
    super(mbox, blob.getDigest(), blob.getRawSize());
    mLocalBlob = blob;
  }

  public VolumeBlob getLocalBlob() {
    return mLocalBlob;
  }

  @Override
  public String getLocator() {
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
