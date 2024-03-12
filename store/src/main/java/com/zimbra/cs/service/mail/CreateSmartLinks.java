package com.zimbra.cs.service.mail;

import com.zextras.mailbox.AuthenticationInfo;
import com.zextras.mailbox.smartlinks.Attachment;
import com.zextras.mailbox.smartlinks.SmartLinksGenerator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateSmartLinksRequest;
import com.zimbra.soap.mail.message.CreateSmartLinksResponse;
import com.zimbra.soap.mail.type.AttachmentToConvert;
import com.zimbra.soap.mail.type.SmartLink;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zimbra.soap.JaxbUtil.elementToJaxb;
import static com.zimbra.soap.JaxbUtil.jaxbToElement;

public class CreateSmartLinks extends MailDocumentHandler {

  private final SmartLinksGenerator smartLinksGenerator;

  public CreateSmartLinks(SmartLinksGenerator smartLinksGenerator) {
    this.smartLinksGenerator = smartLinksGenerator;
  }

  @Override
  public Element handle(Element requestElement, Map<String, Object> context) throws ServiceException {
    final CreateSmartLinksRequest request = elementToJaxb(requestElement);
    final AuthenticationInfo authenticationInfo = getAuthenticationInfo(context);

    final CreateSmartLinksResponse response = handle(request, authenticationInfo);
    return jaxbToElement(response);
  }

  private CreateSmartLinksResponse handle(
      CreateSmartLinksRequest req,
      AuthenticationInfo authenticationInfo
  ) throws ServiceException {
    final List<Attachment> attachments = toAttachments(req.getAttachments());
    final List<SmartLink> smartLinks = generateSmartLinks(authenticationInfo, attachments);
    return new CreateSmartLinksResponse(smartLinks);
  }

  private List<SmartLink> generateSmartLinks(
      AuthenticationInfo authenticationInfo,
      List<Attachment> attachments
  ) throws ServiceException {
    return smartLinksGenerator
        .smartLinksFrom(attachments, authenticationInfo)
        .stream()
        .map(smartLink -> new SmartLink(smartLink.getPublicUrl())
    ).collect(Collectors.toList());
  }

  private List<Attachment> toAttachments(List<AttachmentToConvert> reqAttachments) {
    return reqAttachments
        .stream()
        .map(toConvert -> new Attachment(toConvert.getDraftId(), toConvert.getPartName()))
        .collect(Collectors.toList());
  }

  private static AuthenticationInfo getAuthenticationInfo(Map<String, Object> context) throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final Account authenticatedAccount = getAuthenticatedAccount(zsc);
    final Account requestedAccount = getRequestedAccount(zsc);
    return new AuthenticationInfo(authenticatedAccount, requestedAccount, zsc.getAuthToken());
  }

}
