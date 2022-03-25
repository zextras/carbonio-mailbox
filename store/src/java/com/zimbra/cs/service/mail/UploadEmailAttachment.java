package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.service.FileUploadProvider;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.store.BlobInputStream;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.UploadAttachmentRequest;
import java.util.Map;
import java.util.Objects;

/**
 * Service class to handle email attachment upload.
 *
 * @author davidefrison
 */
public class UploadEmailAttachment extends MailDocumentHandler {

  private static final Log mLog = LogFactory.getLog(UploadEmailAttachment.class);
  private final FileUploadProvider fileUploadProvider;

  public UploadEmailAttachment(
      FileUploadProvider fileUploadProvider) {
    this.fileUploadProvider = fileUploadProvider;
  }
  public interface MyInterface {
    Upload getUpload(String param1, String param2, AuthToken authToken) throws ServiceException;

  }


  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    UploadAttachmentRequest uploadAttachmentRequest = null;
    if (true) {
      throw MailServiceException.NOT_IMPLEMENTED();
    }
    try {
      uploadAttachmentRequest = zsc.elementToJaxb(request);
    } catch (ServiceException e) {
      //TODO: return error
      mLog.debug(e.getMessage());
      throw new SoapFaultException("Malformed request.", request);
    }
    String uploadId = uploadAttachmentRequest.getUploadId();
    Upload up = null;
    try {
      up = fileUploadProvider.getUpload(zsc.getAuthtokenAccountId(), uploadId,
          zsc.getAuthToken());
    } catch (ServiceException e) {
      throw new SoapFaultException("The file with uid " + uploadId + "does not exist.", request);
    }
    String upName = up.getName();
    BlobInputStream upContent = up.getBlobInputStream();
    //TODO: POST to file api with file content and fileName attachment
    if ((Objects.isNull(upName) || Objects.equals("", upName)) && mLog.isDebugEnabled()) {
      mLog.debug("Provided fileName "
          + MailConstants.UPLOAD_EMAIL_ATTACHMENT_REQUEST.getName()
          + " is empty or null.");
    }

    return zsc.createElement(MailConstants.UPLOAD_EMAIL_ATTACHMENT_RESPONSE);
  }
}
