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
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CopyToDriveRequest;
import com.zimbra.soap.mail.message.CopyToDriveResponse;
import io.vavr.API;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import javax.mail.internet.MimePart;

/**
 * Service class to handle copy item to drive.
 *
 * @author davidefrison
 */
public class CopyToDrive extends MailDocumentHandler {

  private static final Log mLog = LogFactory.getLog(CopyToDrive.class);
  private final AttachmentService attachmentService;
  private final FilesClient filesClient;

  public CopyToDrive(
      AttachmentService attachmentService,
      FilesClient filesClient) {
    this.attachmentService = attachmentService;
    this.filesClient = filesClient;
  }

  /**
   * Main method to handle the copy to drive request.
   *
   * @param request request type Element for {@link CopyToDriveRequest}
   * @param context request context
   * @return Element type for {@link CopyToDriveResponse}
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
    CopyToDriveResponse copyToDriveResponse = new CopyToDriveResponse();
    copyToDriveResponse.setNodeId(nodeId);
    return zsc.jaxbToElement(copyToDriveResponse);
  }

  /**
   * Perform call to Files upload API using request input and context.
   *
   * @param request Element request
   * @param context zimbra soap context
   * @return {@link NodeId} response from Flies client
   */
  private Try<NodeId> getNodeId(Element request, ZimbraSoapContext context) {
    // map to request
    Try<CopyToDriveRequest> copyToDriveReq =
        Try.<CopyToDriveRequest>of(() -> context.elementToJaxb(request))
            .onFailure(ex -> mLog.debug(ex.getMessage()))
            .mapFailure(Case($(instanceOf(Exception.class)),
                new SoapFaultException("Malformed request.", "", false)));
    Try<Integer> messageIdTry = copyToDriveReq.mapTry(req -> Integer.parseInt(req.getMessageId()))
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException(MailConstants.A_MESSAGE_ID + " must ne an integer.", "",
                false)));
    Try<MimePart> attachmentTry = API.For(copyToDriveReq, messageIdTry).yield((req, messageId) ->
            attachmentService.getAttachment(context.getAuthtokenAccountId(), context.getAuthToken(),
                messageId, req.getPart()).get())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("File not found.", "", false)));
    // get file content
    Try<InputStream> uploadContentStream = attachmentTry.mapTry(
            attachment -> attachment.getInputStream())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot read file content.", "", true)));
    Try<String> contentTypeTry = attachmentTry.mapTry(attachment -> attachment.getContentType())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot get file content-type.", "", true)));
    Try<String> fileNameTry = attachmentTry.mapTry(attachment -> attachment.getFileName())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot get file name.", "", true)));
    // execute Files api call
    return API.For(attachmentTry, uploadContentStream, fileNameTry, contentTypeTry)
        .yield((attachment, stream, fileName, contentType) ->
            filesClient.uploadFile(context.getAuthToken().toString(), "LOCAL_ROOT", fileName, contentType, stream)
                .onFailure(ex -> mLog.debug(ex.getMessage()))).get();
  }
}
