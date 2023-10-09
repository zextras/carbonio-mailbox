// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.usecase;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.mailbox.client.MailboxHttpClientException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AttachmentService;
import io.vavr.API;
import io.vavr.API.Match.Pattern0;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Part;
import javax.mail.internet.MimePart;
import org.apache.http.HttpStatus;

@Singleton
public class PreviewUseCase {

  private final PreviewClient previewClient;
  private final AttachmentService attachmentService;

  @Inject
  public PreviewUseCase(PreviewClient previewClient, AttachmentService attachmentService) {
    this.previewClient = previewClient;
    this.attachmentService = attachmentService;
  }

  public Try<AttachmentPreview> getAttachmentAndPreview(
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
        attachmentService
            .getAttachment(accountId, authToken, messageId, partNumber)
            .mapFailure(
                API.Case(
                    Pattern0.of(MailboxHttpClientException.class),
                    (clientException) -> {
                      if (Objects.equals(
                          HttpStatus.SC_NOT_FOUND, clientException.getStatusCode())) {
                        return new AttachmentNotFoundException();
                      }
                      return new RuntimeException(clientException.getMessage());
                    }));

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
                                    .getOrElseThrow(ex -> new PreviewError(ex.getMessage()))))
            .flatMap(Function.identity());
    final Try<String> tryFileName = tryAttachment.mapTry(Part::getFileName);
    return API.For(tryFileName, tryPreviewClientResponse)
        .yield(
            (fileName, previewResponse) ->
                new AttachmentPreview(
                    fileName, previewResponse.getMimeType(), previewResponse.getContent()));
  }
}
