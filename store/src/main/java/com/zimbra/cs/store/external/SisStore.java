// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.zimbra.cs.store.Blob;
import java.io.IOException;

/**
 * ContentAddressableStoreManager which supports SIS (single instance server) operations. The store
 * contains a single copy of each unique blob and tracks reference count when more than one object
 * is associated with that blob. Blobs are only deleted when the reference count reaches zero.
 */
public abstract class SisStore extends ContentAddressableStoreManager {

  /**
   * Retrieve a blob from the remote system based on content hash. The remote system is expected to
   * increment reference count if a blob is found
   *
   * @param hash: The content hash of the blob
   * @return a new Blob instance which holds the content from the remote server, or null if none
   *     exists
   * @throws IOException
   */
  public abstract Blob getSisBlob(byte[] hash) throws IOException;

  @Override
  public boolean supports(StoreFeature feature) {
    switch (feature) {
      case SINGLE_INSTANCE_SERVER_CREATE:
        return true;
      default:
        return super.supports(feature);
    }
  }
}
