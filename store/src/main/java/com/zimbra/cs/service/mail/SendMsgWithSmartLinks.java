// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.FilesClient;
import com.zextras.files.client.GraphQLFilesClient;
import com.zextras.mailbox.AuthenticationInfo;
import com.zextras.mailbox.smartlinks.Attachment;
import com.zextras.mailbox.smartlinks.FilesSmartLinksGenerator;
import com.zextras.mailbox.smartlinks.SmartLinksGenerator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.SendMsgResponse;
import com.zimbra.soap.mail.message.SendMsgWithSmartLinksRequest;
import com.zimbra.soap.mail.type.MsgToSend;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zimbra.soap.JaxbUtil.elementToJaxb;
import static com.zimbra.soap.JaxbUtil.jaxbToElement;

public class SendMsgWithSmartLinks extends DocumentHandler {
  private final SmartLinksGenerator smartLinksGenerator;

  public SendMsgWithSmartLinks(FilesClient filesClient) {
    final var graphQLFilesClient = new GraphQLFilesClient(filesClient, new ObjectMapper());
    smartLinksGenerator = new FilesSmartLinksGenerator(graphQLFilesClient);
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final SendMsgWithSmartLinksRequest req = elementToJaxb(request, SendMsgWithSmartLinksRequest.class);
    final var authenticationInfo = getAuthenticationInfo(context);
    final var attachments = toAttachments(req.getSmartLinks());
    final var smartLinks = smartLinksGenerator.smartLinksFrom(attachments, authenticationInfo);
    // TODO: add smart links to body and remove attachments

    return jaxbToElement(this.sendMsg(req.getMsg(), authenticationInfo));
  }

  private SendMsgResponse sendMsg(MsgToSend msgToSend, AuthenticationInfo authenticationInfo) {
    // TODO: use SendMsgWithLinksResponse
    return null;
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