// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;

/** Wrapper around BlobBuilder for package visibility */
public class ExternalBlobBuilder extends BlobBuilder {
  protected ExternalBlobBuilder(Blob targetBlob) {
    super(targetBlob);
  }
}
