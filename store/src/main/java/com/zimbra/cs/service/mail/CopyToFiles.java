package com.zimbra.cs.service.mail;

import static com.google.common.base.Predicates.instanceOf;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.For;
import static java.util.function.Function.identity;

import com.zextras.carbonio.files.FilesClient;
import com.zextras.carbonio.files.entities.NodeId;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.mail.internet.MimePart;

/**
 * Service class to handle copy item to Files.
 *
 * @author davidefrison
 * @since 4.0.7
 */
public class CopyToFiles extends MailDocumentHandler {

  private static final Log mLog = LogFactory.getLog(CopyToFiles.class);
  private final AttachmentService attachmentService;
  private final FilesClient filesClient;

  public CopyToFiles(AttachmentService attachmentService, FilesClient filesClient) {
    this.attachmentService = attachmentService;
    this.filesClient = filesClient;
  }

  /**
   * Main method to handle the copy to drive request.
   *
   * @param request request type Element for {@link CopyToFilesRequest}
   * @param context request context
   * @return Element for {@link CopyToFilesResponse}
   * @throws ServiceException
   */
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final NodeId nodeId =
        Optional.ofNullable(
                getRequestObject(request)
                    .flatMap(
                        copyToFilesRequest ->
                            this.getAttachmentToCopy(copyToFilesRequest, zsc)
                                .flatMap(
                                    attachment ->
                                        Try.withResources(attachment::getInputStream)
                                            .of(
                                                inputStream ->
                                                    For(
                                                            Try.of(
                                                                () ->
                                                                    ZimbraCookie
                                                                            .COOKIE_ZM_AUTH_TOKEN
                                                                        + "="
                                                                        + zsc.getAuthToken()
                                                                            .getEncoded()),
                                                            this.getDestinationFolderId(
                                                                copyToFilesRequest),
                                                            this.getAttachmentName(attachment),
                                                            this.getAttachmentContentType(
                                                                attachment),
                                                            Try.withResources(
                                                                    attachment::getInputStream)
                                                                .of(this::getAttachmentSize)
                                                                .flatMap(identity()))
                                                        .yield(
                                                            (cookie,
                                                                folderId,
                                                                fileName,
                                                                contentType,
                                                                fileSize) ->
                                                                filesClient.uploadFile(
                                                                    cookie,
                                                                    folderId,
                                                                    fileName,
                                                                    contentType,
                                                                    inputStream,
                                                                    fileSize)))))
                    .flatMap(identity())
                    .flatMap(identity())
                    .onFailure(ex -> mLog.error(ex.getMessage()))
                    .mapFailure(
                        Case(
                            $(ex -> !(ex instanceof ServiceException)),
                            ServiceException::INTERNAL_ERROR))
                    .get())
            .orElseThrow(() -> ServiceException.FAILURE("got null response from Files server."));
    CopyToFilesResponse copyToFilesResponse = new CopyToFilesResponse();
    copyToFilesResponse.setNodeId(nodeId.getNodeId());
    return zsc.jaxbToElement(copyToFilesResponse);
  }

  /**
   * Transforms the SOAP request in {@link CopyToFilesRequest} instance
   *
   * @param request soap {@link Element} as received from client
   * @return try of {@link CopyToFilesRequest}
   */
  private Try<CopyToFilesRequest> getRequestObject(Element request) {
    return Try.<CopyToFilesRequest>of(() -> JaxbUtil.elementToJaxb(request))
        .onFailure(ex -> mLog.error(ex.getMessage()))
        .recoverWith(ex -> Try.failure(ServiceException.PARSE_ERROR("Malformed request.", ex)));
  }

  /**
   * Gets an attachment using request and context authorization info.
   *
   * @param request try of {@link CopyToFilesRequest}
   * @param context the context for current session
   * @return try of a {@link MimePart}
   */
  private Try<MimePart> getAttachmentToCopy(CopyToFilesRequest request, ZimbraSoapContext context) {
    // get messageId with UUID if available
    final String[] uuidMsgId = request.getMessageId().split(":");
    String accountUUID;
    String msgId;
    if (Objects.equals(2, uuidMsgId.length)) {
      accountUUID = uuidMsgId[0];
      msgId = uuidMsgId[1];
    } else {
      accountUUID = context.getAuthtokenAccountId();
      msgId = request.getMessageId();
    }
    final Try<Integer> messageIdTry =
        Try.of(() -> Integer.parseInt(msgId))
            .onFailure(ex -> mLog.error(ex.getMessage()))
            .mapFailure(
                Case(
                    $(instanceOf(NumberFormatException.class)),
                    ex ->
                        ServiceException.PARSE_ERROR(
                            MailConstants.A_MESSAGE_ID + " must be an integer.", ex)),
                Case($(instanceOf(Exception.class)), ServiceException::INTERNAL_ERROR));
    return For(Try.of(() -> request), messageIdTry)
        .yield(
            (req, messageId) ->
                attachmentService
                    .getAttachment(accountUUID, context.getAuthToken(), messageId, req.getPart())
                    .onFailure(ex -> mLog.error(ex.getMessage()))
                    .recoverWith(
                        ex -> Try.failure(ServiceException.NOT_FOUND("File not found.", ex))))
        .flatMap(result -> result);
  }

  /**
   * Calculates real size of input stream by reading it. The operation is done by reading chunks of
   * 8kb. This method exists because for images the size returned by {@link MimePart#getSize()} is
   * not equal to the real one.
   *
   * @param inputStream input stream to calculate size of
   * @return size of given input stream
   */
  private Try<Long> getAttachmentSize(InputStream inputStream) {

    return Try.of(
        () -> {
          long fileSize = 0;
          byte[] buffer = new byte[8192];
          for (int read = inputStream.read(buffer); read != -1; read = inputStream.read(buffer)) {
            fileSize += read;
          }
          return fileSize;
        });
  }

  /**
   * Get destination folder parameter from request. In case destination is null or empty a default
   * value is returned.
   *
   * @param request {@link CopyToFilesRequest}
   * @return destination folder id from request
   */
  private Try<String> getDestinationFolderId(CopyToFilesRequest request) {
    // get destinationId
    return Try.of(() -> Optional.ofNullable(request.getDestinationFolderId()))
        .mapTry(
            optional -> {
              String destId = optional.orElse("LOCAL_ROOT");
              return Objects.equals("", destId) ? "LOCAL_ROOT" : destId;
            })
        .onFailure(ex -> mLog.error(ex.getMessage()));
  }

  /**
   * Get an attachment file name with {@link Try} and {@link ServiceException} in case of failure.
   *
   * @param attachment a {@link MimePart} attachment
   * @return attachment file name
   */
  private Try<String> getAttachmentName(MimePart attachment) {
    return Try.of(attachment::getFileName)
        .onFailure(ex -> mLog.error(ex.getMessage()))
        .recoverWith(ex -> Try.failure(ServiceException.FAILURE("Cannot get file name.", ex)));
  }

  /**
   * Get an attachment content type with {@link Try} and {@link ServiceException} in case of
   * failure.
   *
   * @param attachment {@link MimePart} attachment
   * @return try of attachment content type
   */
  private Try<String> getAttachmentContentType(MimePart attachment) {
    return Try.of(attachment::getContentType)
        .onFailure(ex -> mLog.error(ex.getMessage()))
        .recoverWith(
            ex -> Try.failure(ServiceException.FAILURE("Cannot get file content-type.", ex)));
  }
}
