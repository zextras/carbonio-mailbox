// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import static com.zimbra.soap.JaxbUtil.elementToJaxb;
import static com.zimbra.soap.JaxbUtil.jaxbToElement;

import com.zextras.mailbox.AuthenticationInfo;
import com.zextras.mailbox.smartlinks.Attachment;
import com.zextras.mailbox.smartlinks.SmartLink;
import com.zextras.mailbox.smartlinks.SmartLinksGenerator;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.SendMsgRequest;
import com.zimbra.soap.mail.message.SendMsgResponse;
import com.zimbra.soap.mail.message.SendMsgWithSmartLinksRequest;
import com.zimbra.soap.mail.type.MsgToSend;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SendMsgWithSmartLinks extends DocumentHandler {
  private final SmartLinksGenerator smartLinksGenerator;

  public SendMsgWithSmartLinks(SmartLinksGenerator smartLinksGenerator) {
    this.smartLinksGenerator = smartLinksGenerator;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final SendMsgWithSmartLinksRequest req = elementToJaxb(request, SendMsgWithSmartLinksRequest.class);
    final var authenticationInfo = getAuthenticationInfo(context);
    final var attachments = toAttachments(req.getSmartLinks());
    final var smartLinks = smartLinksGenerator.smartLinksFrom(attachments, authenticationInfo);

    final MsgToSend message = req.getMsg();
    final String updatedContent = message.getContent() + smartLinks.stream().map(SmartLink::getPublicUrl).collect(
        Collectors.joining("<br>"));
    message.setContent(updatedContent);
    return jaxbToElement(this.sendMsg(message, authenticationInfo));
  }

  private SendMsgResponse sendMsg(MsgToSend msgToSend, AuthenticationInfo authenticationInfo) throws ServiceException {
    Account authenticatedAccount = authenticationInfo.getAuthenticatedAccount();
    Account requestedAccount = authenticationInfo.getRequestedAccount();
    ZAuthToken zAuthToken = authenticationInfo.getAuthToken().toZAuthToken();
    String soapUrl = URLUtil.getSoapURL(authenticatedAccount.getServer(), true);
    SendMsgRequest request = new SendMsgRequest();
    request.setMsg(msgToSend);
    final Element autocompleteRequestElement = JaxbUtil.jaxbToElement(request);
    try {
      return JaxbUtil.elementToJaxb(
          new SoapHttpTransport(zAuthToken, soapUrl)
              .invoke(autocompleteRequestElement, requestedAccount.getId()),
          SendMsgResponse.class);
    } catch (IOException e) {
      throw ServiceException.FAILURE(e.getMessage());
    }
  }

  private static List<Attachment> toAttachments(List<com.zimbra.soap.mail.type.SmartLink> smartLinks1) {
    return smartLinks1.stream().map(smartLink -> new Attachment(smartLink.getDraftId(), smartLink.getPartName())).collect(Collectors.toList());
  }

  private static AuthenticationInfo getAuthenticationInfo(Map<String, Object> context) throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final Account authenticatedAccount = getAuthenticatedAccount(zsc);
    final Account requestedAccount = getRequestedAccount(zsc);
    return new AuthenticationInfo(authenticatedAccount, requestedAccount, zsc.getAuthToken());
  }
}