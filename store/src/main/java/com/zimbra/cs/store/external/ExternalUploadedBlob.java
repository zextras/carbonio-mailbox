// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.zimbra.cs.store.Blob;
import java.io.IOException;

/**
 * Blob which has been streamed to a remote store while also being written to local incoming cache.
 * Used during streaming upload to optimize the stage operation.
 */
public class ExternalUploadedBlob extends ExternalBlob {
  protected final String uploadId;

  /**
   * Create a new ExternalUploadedBlob from data which was written directly to remote server during
   * upload
   *
   * @param blob: The local Blob which was created inline with upload
   * @param uploadId: The remote system's identifier for the upload
   * @throws IOException
   */
  protected ExternalUploadedBlob(Blob blob, String uploadId) throws IOException {
    super(blob.getFile(), blob.getRawSize(), blob.getDigest());
    this.uploadId = uploadId;
  }

  /**
   * @return the remote system's identifier for this uploaded blob
   */
  public String getUploadId() {
    return uploadId;
  }
}
