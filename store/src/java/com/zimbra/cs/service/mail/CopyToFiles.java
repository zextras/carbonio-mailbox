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
    // throw exception if failure or nodeId null
    String nodeId = Optional.ofNullable(
            this.getNodeId(request, zsc)
                .getOrElseThrow(ex -> new SoapFaultException("Service failure.", "", true)))
        .orElseThrow(() -> new SoapFaultException("Service failure.", "", true))
        .getNodeId();
    CopyToFilesResponse copyToFilesResponse = new CopyToFilesResponse();
    copyToFilesResponse.setNodeId(nodeId);
    return zsc.jaxbToElement(copyToFilesResponse);
  }

  /**
   * Perform call to Files upload API using request input and context.
   *
   * @param request Element request
   * @param context zimbra soap context
   * @return {@link NodeId} response from Flies client
   */
  private Try<NodeId> getNodeId(Element request, ZimbraSoapContext context) {
    // get auth cookie
    Try<String> authCookieTry = Try.of(() -> ZimbraCookie.COOKIE_ZM_AUTH_TOKEN + "=" + context.getAuthToken().getEncoded());

    // map to request
    Try<CopyToFilesRequest> copyToDriveReq =
        Try.<CopyToFilesRequest>of(() -> context.elementToJaxb(request))
            .onFailure(ex -> mLog.debug(ex.getMessage()))
            .mapFailure(Case($(instanceOf(Exception.class)),
                new SoapFaultException("Malformed request.", "", false)));
    // get mail message
    Try<Integer> messageIdTry = copyToDriveReq.mapTry(req -> Integer.parseInt(req.getMessageId()))
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException(MailConstants.A_MESSAGE_ID + " must ne an integer.", "",
                false)));
    // get mail attachment
    Try<MimePart> attachmentTry = API.For(copyToDriveReq, messageIdTry).yield((req, messageId) ->
            attachmentService.getAttachment(context.getAuthtokenAccountId(), context.getAuthToken(),
                messageId, req.getPart()).get())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("File not found.", "", false)));
    // get attachment content as stream
    Try<InputStream> attachmentStream = attachmentTry.mapTry(
            attachment -> attachment.getInputStream())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot read file content.", "", true)));
    // get attachment content-type
    Try<String> contentTypeTry = attachmentTry.mapTry(attachment -> attachment.getContentType())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot get file content-type.", "", true)));
    // get attachment name
    Try<String> fileNameTry = attachmentTry.mapTry(attachment -> attachment.getFileName())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot get file name.", "", true)));
    // get attachment size (have to read the attachment)
    Try<Long> attachmentSize = attachmentTry.mapTry(
            attachment -> (long) attachment.getSize())
        .onFailure(ex -> mLog.debug(ex.getMessage()));

    // execute Files api call with all previous values
    return API.For(authCookieTry, attachmentTry, attachmentStream, attachmentSize, fileNameTry, contentTypeTry)
        .yield((authCookie, attachment, stream, streamSize, fileName, contentType) ->
            filesClient.uploadFile(authCookie, "LOCAL_ROOT", fileName, contentType, stream, streamSize)
                .onFailure(ex -> mLog.debug(ex.getMessage()))).get();
  }
}
