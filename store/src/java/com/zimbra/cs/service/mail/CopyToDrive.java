package com.zimbra.cs.service.mail;

import static io.vavr.API.$;

import com.zextras.carbonio.files.FilesClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.FileUploadProvider;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CopyToDriveRequest;
import com.zimbra.soap.mail.message.CopyToDriveResponse;

import io.vavr.control.Option;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
   * Specification to get an uploaded attachment in the mailbox
   */
  public interface MyInterface {
    Upload getUpload(String param1, String param2, AuthToken authToken) throws ServiceException;
  }


  /**
   * Handles the copy to drive request
   * @param request request type Element for {@link CopyToDriveRequest}
   * @param context request context
   * @return Element type for {@link CopyToDriveResponse}
   * @throws ServiceException
   */
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);
    CopyToDriveRequest jaxbEl = JaxbUtil.elementToJaxb(request);
    try {
      jaxbEl = zsc.elementToJaxb(request);
    } catch (ServiceException e) {
      mLog.debug(e.getMessage());
      throw new SoapFaultException("Malformed request.", request);
    }
    String uploadId = jaxbEl.getUploadId();
    Upload up = null;
    try {
      up = fileUploadProvider.getUpload(zsc.getAuthtokenAccountId(), uploadId,
          zsc.getAuthToken());
    } catch (ServiceException e) {
      throw new SoapFaultException("The file with uid " + uploadId + " does not exist.", request);
    }
    String upName = up.getName();
    InputStream upContent = null;
    try {
      upContent = up.getInputStream();
    } catch (IOException e) {
      throw new SoapFaultException("The file with uid " + uploadId + " is unreadable.", request);
    }
    String cookie = zsc.getAuthToken().toString();
    if ((Objects.isNull(upName) || Objects.equals("", upName)) && mLog.isDebugEnabled()) {
      mLog.debug("Provided fileName "
          + MailConstants.COPY_TO_DRIVE_REQUEST.getName()
          + " is empty or null.");
    }
    String nodeId = Optional.ofNullable(filesClient.uploadFile(cookie, "LOCAL_ROOT",
            up.getName(), up.getContentType(), upContent)
        .getOrElseThrow(() -> new SoapFaultException("Service failure.", request)))
        .orElseThrow(() -> new SoapFaultException("Service failure.", request))
        .getNodeId();
        ;
    return zsc.createElement(MailConstants.COPY_TO_DRIVE_RESPONSE);
  }
}
