package com.zimbra.cs.service.mail;

import com.zextras.mailbox.AuthenticationInfo;
import com.zextras.mailbox.smartlinks.Attachment;
import com.zextras.mailbox.smartlinks.SmartLinksGenerator;
import com.zextras.mailbox.tracking.Event;
import com.zextras.mailbox.tracking.Tracking;
import com.zextras.mailbox.tracking.TrackingUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateSmartLinksRequest;
import com.zimbra.soap.mail.message.CreateSmartLinksResponse;
import com.zimbra.soap.mail.type.AttachmentToConvert;
import com.zimbra.soap.mail.type.SmartLink;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class CreateSmartLinks extends MailDocumentHandler {

  private final SmartLinksGenerator smartLinksGenerator;
  private final Tracking tracking;

  public CreateSmartLinks(SmartLinksGenerator smartLinksGenerator,
      Tracking tracking) {
    this.smartLinksGenerator = smartLinksGenerator;
    this.tracking = tracking;
  }

  @Override
  public Element handle(Element requestElement, Map<String, Object> context) throws ServiceException {
    final var soapContext = getZimbraSoapContext(context);
    final var authenticationInfo = getAuthenticationInfo(context);
    final CreateSmartLinksRequest request = soapContext.elementToJaxb(requestElement);

    final var response = handle(request, authenticationInfo);

    return soapContext.jaxbToElement(response);
  }

  private CreateSmartLinksResponse handle(
      CreateSmartLinksRequest req,
      AuthenticationInfo authenticationInfo
  ) throws ServiceException {
    if (req.getAttachments() == null || req.getAttachments().isEmpty()) {
      throw ServiceException.INVALID_REQUEST("No attachment has been specified.", new NoSuchElementException());
    }
    final List<Attachment> attachments = toAttachments(req.getAttachments());
    final List<SmartLink> smartLinks = generateSmartLinks(authenticationInfo, attachments);

    final String uid = TrackingUtil.anonymize(authenticationInfo.getAuthenticatedAccount().getId());
    tracking.sendEvent(new Event(uid, "Mail", "SendEmailWithSmartLink"));

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
