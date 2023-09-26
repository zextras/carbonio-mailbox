// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.usecase;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AttachmentService;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.internet.MimePart;

@Singleton
public class PreviewUseCase {

  private final PreviewClient previewClient;
  private final AttachmentService attachmentService;

  @Inject
  public PreviewUseCase(PreviewClient previewClient, AttachmentService attachmentService) {
    this.previewClient = previewClient;
    this.attachmentService = attachmentService;
  }

  public Try<Tuple2<MimePart, BlobResponse>> getAttachmentAndPreview(
      String accountId,
      AuthToken authToken,
      PreviewType previewType,
      int messageId,
      String partNumber,
      Query query) {
    if (!previewClient.healthLive()) {
      return Try.failure(new PreviewNotHealthy());
    }

    final Try<MimePart> tryAttachment =
        attachmentService.getAttachment(accountId, authToken, messageId, partNumber);

    final Try<BlobResponse> tryPreviewClientResponse =
        tryAttachment
            .mapTry(
                attachment ->
                    Try.withResources(attachment::getInputStream)
                        .of(
                            inputStream ->
                                previewType
                                    .getFunction()
                                    .apply(
                                        previewClient, inputStream, query, attachment.getFileName())
                                    .getOrElseThrow(
                                        throwable -> new PreviewError(throwable.getMessage()))))
            .flatMap(Function.identity());
    return Try.of(() -> new Tuple2<>(tryAttachment.get(), tryPreviewClientResponse.get()));
  }
}
