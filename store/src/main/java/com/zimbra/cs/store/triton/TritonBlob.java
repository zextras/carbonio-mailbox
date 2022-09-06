// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.triton;

import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.external.ExternalUploadedBlob;
import java.io.IOException;

/**
 * Blob wrapper which includes a previously generated Locator Used to hold locator calculated during
 * streaming upload so stage operation does not need to recalculate it
 */
public class TritonBlob extends ExternalUploadedBlob {
  private final String locator;
  private final MozyServerToken serverToken;

  protected TritonBlob(Blob blob, String locator, String uploadUrl, MozyServerToken serverToken)
      throws IOException {
    super(blob, uploadUrl);
    this.locator = locator;
    this.serverToken = serverToken;
  }

  public String getLocator() {
    return locator;
  }

  public MozyServerToken getServerToken() {
    return serverToken;
  }
}
