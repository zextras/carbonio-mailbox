package com.zimbra.cs.service.mail;

import static com.google.common.base.Predicates.instanceOf;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.For;

import com.zextras.carbonio.files.FilesClient;
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
import javax.mail.Part;
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
    // get auth token
    final Try<String> authCookieTry =
        Try.of(() -> ZimbraCookie.COOKIE_ZM_AUTH_TOKEN + "=" + zsc.getAuthToken().getEncoded());
    // map element to copy-to-files request object
    final Try<CopyToFilesRequest> copyToFilesRequestTry = getRequestObject(request);
    // get Files destination folder
    final Try<String> destFolderIdTry = copyToFilesRequestTry.flatMap(this::getDestinationFolderId);
    // get attachment
    final Try<MimePart> attachmentTry =
        copyToFilesRequestTry.flatMap(
            copyToFilesRequest -> getAttachmentToCopy(copyToFilesRequest, zsc));
    // get attachment size
    final Try<Long> attachmentSizeTry =
        attachmentTry.mapTry(Part::getInputStream).flatMap(this::getAttachmentSize);
    // get attachment content-type
    final Try<String> contentTypeTry = attachmentTry.flatMap(this::getAttachmentContentType);
    // get attachment name
    final Try<String> fileNameTry = attachmentTry.flatMapTry(this::getAttachmentName);
    // get attachment content stream
    final Try<InputStream> attachmentStreamTry =
        attachmentTry.mapTry(Part::getInputStream).onFailure(ex -> mLog.debug(ex.getMessage()));

    // execute Files api call and flatten yield result
    // maps every non-ServiceException to internal error
    String nodeId =
        Optional.ofNullable(
                For(
                        authCookieTry,
                        destFolderIdTry,
                        fileNameTry,
                        contentTypeTry,
                        attachmentStreamTry,
                        attachmentSizeTry)
                    .yield(
                        (authCookie,
                            destFolderId,
                            fileName,
                            contentType,
                            attachmentStream,
                            attachmentSize) ->
                            filesClient.uploadFile(
                                authCookie,
                                destFolderId,
                                fileName,
                                contentType,
                                attachmentStream,
                                attachmentSize))
                    .flatMap(x -> x)
                    .onFailure(ex -> mLog.debug(ex.getMessage()))
                    .mapFailure(
                        Case(
                            $(ex -> !(ex instanceof ServiceException)),
                            ex -> ServiceException.FAILURE("internal error.", ex)))
                    .get())
            .orElseThrow(() -> ServiceException.FAILURE("got null response from Files server."))
            .getNodeId();

    CopyToFilesResponse copyToFilesResponse = new CopyToFilesResponse();
    copyToFilesResponse.setNodeId(nodeId);
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
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(
            Case(
                $(instanceOf(Exception.class)),
                ex -> ServiceException.PARSE_ERROR("Malformed request.", ex)));
  }

  /**
   * Gets an attachment using request and context authorization info.
   *
   * @param request try of {@link CopyToFilesRequest}
   * @param context the context for current session
   * @return try of a {@link MimePart}
   */
  private Try<MimePart> getAttachmentToCopy(CopyToFilesRequest request, ZimbraSoapContext context) {
    // get mail message
    final Try<Integer> messageIdTry =
        Try.of(() -> Integer.parseInt(request.getMessageId()))
            .onFailure(ex -> mLog.debug(ex.getMessage()))
            .mapFailure(
                Case(
                    $(instanceOf(Exception.class)),
                    ex ->
                        ServiceException.PARSE_ERROR(
                            MailConstants.A_MESSAGE_ID + " must be an integer.", ex)));
    // get mail attachment
    return For(Try.of(() -> request), messageIdTry)
        .yield(
            (req, messageId) ->
                attachmentService
                    .getAttachment(
                        context.getAuthtokenAccountId(),
                        context.getAuthToken(),
                        messageId,
                        req.getPart())
                    .onFailure(ex -> mLog.debug(ex.getMessage()))
                    .mapFailure(
                        Case(
                            $(instanceOf(Exception.class)),
                            ex -> ServiceException.NOT_FOUND("File not found.", ex))))
        .flatMap(result -> result);
  }

  /**
   * Calculates size of input stream by reading it. The operation is done by reading chunks of 8kb.
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
        .onFailure(ex -> mLog.debug(ex.getMessage()));
  }

  /**
   * Get an attachment file name with {@link Try} and {@link ServiceException} in case of failure.
   *
   * @param attachment a {@link MimePart} attachment
   * @return attachment file name
   */
  private Try<String> getAttachmentName(MimePart attachment) {
    return Try.of(() -> attachment.getFileName())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(
            Case(
                $(instanceOf(Exception.class)),
                ex -> ServiceException.FAILURE("Cannot get file name.", ex)));
  }

  /**
   * Get an attachment content type with {@link Try} and {@link ServiceException} in case of
   * failure.
   *
   * @param attachment {@link MimePart} attachment
   * @return try of attachment content type
   */
  private Try<String> getAttachmentContentType(MimePart attachment) {
    return Try.of(() -> attachment.getContentType())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(
            Case(
                $(instanceOf(Exception.class)),
                ex -> ServiceException.FAILURE("Cannot get file content-type.", ex)));
  }
}
