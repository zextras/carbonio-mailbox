// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.octosync;

import com.zimbra.common.service.ServiceException;
import java.io.InputStream;

/**
 * Provides access to files referenced from a patch processed by PatchInputStream.
 *
 * <p>The type does not purport to become universal way to access blobs, rather to abstract out this
 * functionality from OpatchInputStream only.
 */
public interface BlobAccess {

  /**
   * Gets the blob input stream for specified file/version.
   *
   * @param fileId The file id
   * @param version The version
   * @return The blob input stream
   * @throws ServiceException The service exception
   * @throws InvalidPatchReferenceException Patch contains reference that cannot be resolved
   */
  public abstract InputStream getBlobInputStream(int fileId, int version)
      throws ServiceException, InvalidPatchReferenceException;

  /**
   * For given file id/version returns the actual file id and version that will be referenced by
   * this BlobAccess.
   *
   * @param fileId File id as referenced
   * @param version The verison number
   * @return Array with file id (0 index) and version (1 index) to reference
   */
  public abstract int[] getActualReference(int fileId, int version);
}
