// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zextras.mailbox.AuthenticationInfo;
import com.zextras.mailbox.smartlinks.Attachment;
import com.zextras.mailbox.smartlinks.SmartLink;
import com.zextras.mailbox.smartlinks.SmartLinksGenerator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.cs.account.Account;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.SendMsgRequest;
import com.zimbra.soap.mail.message.SendMsgResponse;
import com.zimbra.soap.mail.message.SendMsgWithSmartLinksRequest;
import com.zimbra.soap.mail.type.MsgToSend;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zimbra.cs.httpclient.URLUtil.getSoapURL;
import static com.zimbra.soap.JaxbUtil.elementToJaxb;
import static com.zimbra.soap.JaxbUtil.jaxbToElement;
import static java.util.stream.Collectors.joining;

public class SendMsgWithSmartLinks extends DocumentHandler {
  private final SmartLinksGenerator smartLinksGenerator;

  public SendMsgWithSmartLinks(SmartLinksGenerator smartLinksGenerator) {
    this.smartLinksGenerator = smartLinksGenerator;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final SendMsgWithSmartLinksRequest req = elementToJaxb(request, SendMsgWithSmartLinksRequest.class);
    final var authenticationInfo = getAuthenticationInfo(context);
//    final var attachments = toAttachments(req.getSmartLinks());
//    final var smartLinks = smartLinksGenerator.smartLinksFrom(attachments, authenticationInfo);
    final var smartLinks = List.of(
        new SmartLink("https://public-url.local/1"),
        new SmartLink("https://public-url.local/2")
    );
    final var smartLinksContent = smartLinks.stream().map(SmartLink::getPublicUrl).collect(joining("<br>"));
    final var message = req.getMsg();

    message.getMimePart()
        .getMimeParts()
        .forEach((mimePart) -> {
          // TODO: check the mimePart.getContentType() // text/plain | text/html
          // TODO: for html should be put inside html tags and before signature
          mimePart.setContent(mimePart.getContent() + smartLinksContent);
        });

    final var response = sendMsg(message, authenticationInfo);
    return jaxbToElement(response);
  }

  private SendMsgResponse sendMsg(MsgToSend msgToSend, AuthenticationInfo authenticationInfo) throws ServiceException {
    final var authenticatedAccount = authenticationInfo.getAuthenticatedAccount();
    final var requestedAccount = authenticationInfo.getRequestedAccount();
    final var zAuthToken = authenticationInfo.getAuthToken().toZAuthToken();
    final var soapUrl = getSoapURL(authenticatedAccount.getServer(), true);
    final var request = new SendMsgRequest();
    request.setMsg(msgToSend);

    try {
      final var soapRequest = jaxbToElement(request);
      final var soapResponse = new SoapHttpTransport(zAuthToken, soapUrl).invoke(soapRequest, requestedAccount.getId());
      return elementToJaxb(soapResponse, SendMsgResponse.class);
    } catch (IOException e) {
      throw ServiceException.FAILURE(e.getMessage());
    }
  }

  private static List<Attachment> toAttachments(List<com.zimbra.soap.mail.type.SmartLink> smartLinks) {
    return smartLinks.stream()
        .map(smartLink -> new Attachment(smartLink.getDraftId(), smartLink.getPartName()))
        .collect(Collectors.toList());
  }

  private static AuthenticationInfo getAuthenticationInfo(Map<String, Object> context) throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final Account authenticatedAccount = getAuthenticatedAccount(zsc);
    final Account requestedAccount = getRequestedAccount(zsc);
    return new AuthenticationInfo(authenticatedAccount, requestedAccount, zsc.getAuthToken());
  }
}