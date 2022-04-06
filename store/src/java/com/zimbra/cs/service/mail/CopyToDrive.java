package com.zimbra.cs.service.mail;

import static com.google.common.base.Predicates.instanceOf;
import static io.vavr.API.$;
import static io.vavr.API.Case;

import com.zextras.carbonio.files.FilesClient;
import com.zextras.carbonio.files.entities.NodeId;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.service.FileUploadProvider;
import com.zimbra.cs.service.FileUploadServlet.Upload;
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
  private final FileUploadProvider fileUploadProvider;
  private final FilesClient filesClient;

  public CopyToDrive(
      FileUploadProvider fileUploadProvider,
      FilesClient filesClient) {
    this.fileUploadProvider = fileUploadProvider;
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
   * Get attachment for a mail message on a mailbox given account and authorization info.
   * TODO: consider making this method part of an AttachmentService or similar (or MimeService)
   *
   * @param accountId id of the account that is requesting the attachment
   * @param token authorization token for the account
   * @param messageId id of the message where attachment lies
   * @param part part number that identifies attachment in the mail
   * @return try of {@link javax.mail.internet.MimePart}
   */
  private Try<MimePart> getMessageAttachment(String accountId, AuthToken token, int messageId, String part) {
    return Try.of(() -> MailboxManager.getInstance().getMailboxByAccountId(accountId))
        .mapTry(mailbox -> mailbox.getMessageById(new OperationContext(token), messageId))
        .mapTry(message -> Mime.getMimePart(message.getMimeMessage(), part));
  }

  /**
   * Perform call to Files upload API using request input and context.
   *
   * @param request Element request
   * @param context zimbra soap context
   * @return {@link NodeId} response from Flies client
   * @throws SoapFaultException
   */
  private Try<NodeId> getNodeId(Element request, ZimbraSoapContext context)
      throws SoapFaultException {
    // map to request
    Try<CopyToDriveRequest> copyToDriveReq =
        Try.<CopyToDriveRequest>of(() -> context.elementToJaxb(request))
            .onFailure(ex -> mLog.debug(ex.getMessage()))
            .mapFailure(Case($(instanceOf(Exception.class)),
                new SoapFaultException("Malformed request.", "", false)));
    // get file from mailbox
    Try<MimePart> upload = API.For(copyToDriveReq).yield(req ->
            getMessageAttachment(context.getAuthtokenAccountId(), context.getAuthToken(),
                Integer.parseInt(req.getMessageId()), req.getPart()).get())
            .onFailure(ex -> mLog.debug(ex.getMessage()))
            .mapFailure(Case($(instanceOf(Exception.class)),
                new SoapFaultException("File not found.", "", false)));
    // get file content
    Try<InputStream> uploadContentStream = upload.mapTry(up -> up.getInputStream())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot read file content.", "", true)));
    Try<String> contentTypeTry = upload.mapTry(up -> up.getContentType())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot get file content-type.", "", true)));
    Try<String> fileNameTry = upload.mapTry(up -> up.getFileName())
        .onFailure(ex -> mLog.debug(ex.getMessage()))
        .mapFailure(Case($(instanceOf(Exception.class)),
            new SoapFaultException("Cannot get file name.", "", true)));
    // execute Files api call
    return API.For(upload, uploadContentStream, fileNameTry, contentTypeTry).yield((up, stream, fileName, contentType) ->
        filesClient.uploadFile(context.getAuthToken().toString(), "LOCAL_ROOT", fileName,
                contentType, stream)
            .onFailure(ex -> mLog.debug(ex.getMessage()))).get();
  }

  /**
   * Specification to get an uploaded attachment in the mailbox
   */
  public interface LocalFileUploadGetter {

    /**
     * Get an uploaded item from the mailbox.
     *
     * @param accountId id of the account who is performing the request
     * @param fileId    id of the file to get
     * @param authToken auth token of the logged in account
     * @return the retrieved {@link Upload} item
     * @throws ServiceException exception in case underlying service failed
     */
    Upload getUpload(String accountId, String fileId, AuthToken authToken) throws ServiceException;
  }
}
