// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.usecase;

import java.io.InputStream;

/**
 * Representation of an attachment Preview. It is a mix of a Mailbox attachment and the preview of
 * it received from {@link com.zextras.carbonio.preview.PreviewClient}.
 */
public class AttachmentPreview {

  private final String fileName;
  private final String mimeType;
  private final InputStream content;

  public AttachmentPreview(String fileName, String mimeType, InputStream content) {
    this.fileName = fileName;
    this.mimeType = mimeType;
    this.content = content;
  }

  public String getFileName() {
    return fileName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public InputStream getContent() {
    return content;
  }
}
