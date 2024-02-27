package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CreateSmartLinksRequest;
import io.vavr.control.Try;
import java.util.Map;

public class CreateSmartLinks extends MailDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    var aa = getRequestObject(request).get();
    return JaxbUtil.jaxbToElement(aa);
  }

  private Try<CreateSmartLinksRequest> getRequestObject(Element request) {
    return Try.<CreateSmartLinksRequest>of(() -> JaxbUtil.elementToJaxb(request))
        .recoverWith(ex -> Try.failure(ServiceException.PARSE_ERROR("Malformed request.", ex)));
  }



}
