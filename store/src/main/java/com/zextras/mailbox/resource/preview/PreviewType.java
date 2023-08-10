// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.preview;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import io.vavr.Function4;
import io.vavr.control.Try;
import java.io.InputStream;

public enum PreviewType {
  PDF(PreviewClient::postPreviewOfPdf),
  PDF_THUMBNAIL(PreviewClient::postThumbnailOfPdf),
  DOC(PreviewClient::postPreviewOfDocument),
  DOC_THUMBNAIL(PreviewClient::postThumbnailOfDocument),
  IMAGE(PreviewClient::postPreviewOfImage),
  IMAGE_THUMBNAIL(PreviewClient::postThumbnailOfImage);

  private final Function4<PreviewClient, InputStream, Query, String, Try<BlobResponse>> function;

  PreviewType(Function4<PreviewClient, InputStream, Query, String, Try<BlobResponse>> function) {
    this.function = function;
  }

  public Function4<PreviewClient, InputStream, Query, String, Try<BlobResponse>> getFunction() {
    return function;
  }
}
