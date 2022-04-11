package com.zimbra.cs.service.mail;

import static com.google.common.base.Predicates.instanceOf;
import static io.vavr.API.$;
import static io.vavr.API.Case;

import com.zextras.carbonio.files.FilesClient;
import com.zextras.carbonio.files.entities.NodeId;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import io.vavr.API;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import javax.mail.internet.MimePart;

/**
 * Service class to handle copy item to Files.
 *
 * @author davidefrison
 */
public class CopyToFiles extends MailDocumentHandler {

  private static final Log mLog = LogFactory.getLog(CopyToFiles.class);
  private final AttachmentService attachmentService;
  private final FilesClient filesClient;

  public CopyToFiles(
      AttachmentService attachmentService,
      FilesClient filesClient) {
    this.attachmentService = attachmentService;
    this.filesClient = filesClient;
  }

  /**
   * Main method to handle the copy to drive request.
   *
   * @param request request type Element for {@link CopyToFilesRequest}
   * @param context request context
   * @return Element type for {@link CopyToFilesResponse}
   * @throws ServiceException
   */
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Try<CopyToFilesRequest> copyToFilesRequestTry = getRequestObject(request);
    Try<MimePart> mimePartTry = copyToFilesRequestTry.flatMap(
        copyToFilesRequest -> getAttachmentToCopy(copyToFilesRequest, zsc));
    String nodeId = Optional.ofNullable(
            API.For(mimePartTry, copyToFilesRequestTry, Try.success(zsc))
                .yield((mimePart, req, ctx) -> copyToFiles(mimePart, req, ctx).get())
                .mapFailure(Case($(ex -> !(ex instanceof SoapFaultException)),
                    new SoapFaultException("Service failure.", "", true)))
                .get())
        .orElseThrow(() -> new SoapFaultException("Service failure.", "", true))
        .getNodeId();

    CopyToFilesResponse copyToFilesResponse = new CopyToFilesResponse();
    copyToFilesResponse.setNodeId(nodeId);
    return zsc.jaxbToElement(copyToFilesResponse);
  }

  /**
   * Transforms the SOAP request in object representation
   *
   * @param request soap {@link Element} as received from client
   * @return try of {@link CopyToFilesRequest}
   */
  Try<CopyToFilesRequest> getRequestObject(Element request) {
    return Try.<CopyToFilesRequest>of(() -> JaxbUtil.elementToJaxb(request))
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Malformed request.", "", false)));
  }

  /**
   * Get attachment using request and context authorization info.
   *
   * @param request try of {@link CopyToFilesRequest}
   * @param context
   * @return
   */
  Try<MimePart> getAttachmentToCopy(CopyToFilesRequest request, ZimbraSoapContext context) {
    // get mail message
    Try<Integer> messageIdTry = Try.of(() -> Integer.parseInt(request.getMessageId()))
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException(MailConstants.A_MESSAGE_ID + " must be an integer.", "",
                false)));
    // get mail attachment
    return API.For(Try.of(() -> request), messageIdTry).yield((req, messageId) ->
        attachmentService.getAttachment(context.getAuthtokenAccountId(), context.getAuthToken(),
                messageId, req.getPart())
            .onFailure(ex -> mLog.debug(ex.getMessage()))
            .mapFailure(Case($(instanceOf(Exception.class)),
                new SoapFaultException("File not found.", "", false)))).get();
  }

  /**
   * Perform call to Files upload API using request input and context.
   *
   * @param attachment a {@link MimePart} object instance
   * @param context    zimbra soap context
   * @return {@link NodeId} response from Flies client
   */
  Try<NodeId> copyToFiles(MimePart attachment, CopyToFilesRequest request,
      ZimbraSoapContext context) {
    // get auth cookie
    Try<String> authCookieTry = Try.of(
        () -> ZimbraCookie.COOKIE_ZM_AUTH_TOKEN + "=" + context.getAuthToken().getEncoded());
    // get attachment content as stream
    Try<InputStream> attachmentStream = attachmentService.getAttachmentRawContent(attachment)
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot read file content.", "", true)));
    // get attachment content-type
    Try<String> contentTypeTry = Try.of(() -> attachment.getContentType())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot get file content-type.", "", true)));
    // get attachment name
    Try<String> fileNameTry = Try.of(() -> attachment.getFileName())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot get file name.", "", true)));
    // get attachment size (have to read the attachment)
    Try<Long> attachmentSize = Try.of(
            () -> (long) attachment.getSize())
        .onFailure(ex -> mLog.debug(ex.getMessage()));
    // get destinationId
    Try<String> destFolderIdTry = Try.of(() ->
            Optional.ofNullable(request.getDestinationFolderId()))
        .mapTry(optional -> optional.orElseThrow(() -> new SoapFaultException(MailConstants.A_DESTINATION_FOLDER_ID + " must not be null", "", true)))
        .onFailure(ex -> mLog.debug(ex.getMessage()));

    // execute Files api call
    return API.For(authCookieTry, destFolderIdTry, attachmentStream, attachmentSize, fileNameTry, contentTypeTry)
        .yield((authCookie, destFolderId, stream, streamSize, fileName, contentType) ->
            filesClient.uploadFile(authCookie, destFolderId, fileName, contentType, stream,
                    streamSize).get())
                .onFailure(ex -> mLog.debug(ex.getMessage()));
  }
}
