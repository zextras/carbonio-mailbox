package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;
import java.util.Objects;

/**
 * Service class to handle email attachment upload.
 *
 * @author davidefrison
 */
public class UploadEmailAttachment extends MailDocumentHandler {

  private static Log mLog = LogFactory.getLog(UploadEmailAttachment.class);

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    String fileName = request.getAttribute(MailConstants.E_ATTACH);
    if ((Objects.isNull(fileName) || Objects.equals("", fileName)) && mLog.isDebugEnabled()) {
      mLog.debug("Provided fileName "
          + MailConstants.UPLOAD_EMAIL_ATTACHMENT_REQUEST.getName()
          + " is empty or null.");
    }
    //TODO: add drive api to upload attachment

    return zsc.createElement(MailConstants.UPLOAD_EMAIL_ATTACHMENT_RESPONSE);
  }
}
