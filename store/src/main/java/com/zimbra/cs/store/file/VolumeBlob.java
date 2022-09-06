// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import com.zimbra.cs.store.Blob;
import java.io.File;

class VolumeBlob extends Blob {
  private final short volumeId;

  VolumeBlob(File file, short volumeId) {
    super(file);
    this.volumeId = volumeId;
  }

  short getVolumeId() {
    return volumeId;
  }
}
